package org.syncloud.android.activity.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.app.OwncloudManager;
import org.syncloud.app.InsiderManager;
import org.syncloud.model.Device;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;


public class Owncloud extends Activity {

    private Device device;
    private ProgressDialog progress;
    private TextView url;
    private LinearLayout loginRow;
    private LinearLayout passRow;
    private Button activateBtn;
    private Button webBtn;
    private Button mobileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_owncloud);

        progress = new ProgressDialog(this);
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);

        url = (TextView) findViewById(R.id.owncloud_url);
        loginRow = (LinearLayout) findViewById(R.id.owncloud_login_row);
        passRow = (LinearLayout) findViewById(R.id.owncloud_pass_row);
        activateBtn = (Button) findViewById(R.id.owncloud_activate);

        webBtn = (Button) findViewById(R.id.owncloud_web_btn);
        mobileBtn = (Button) findViewById(R.id.owncloud_mobile_btn);

        setVisibility(View.GONE, View.GONE);

        status();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.owncloud, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void activate(View view) {

        EditText loginText = (EditText) findViewById(R.id.login);
        EditText passText = (EditText) findViewById(R.id.pass);
        //TODO: Some validation

        String login = loginText.getText().toString();
        String pass = passText.getText().toString();


        finishSetupAsync(device, login, pass);

    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(error);
                progress.setCancelable(true);
            }
        });
    }

    private void status() {
        progress.setMessage("Checking ownCloud status ...");
        progress.show();

        AsyncTask.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        final Result<Optional<String>> result = OwncloudManager.owncloudUrl(device);

                        if (result.hasError()) {
                            showError(result.getError());
                            return;
                        }


                        if (result.getValue().isPresent()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    url.setText(result.getValue().get());
                                    setVisibility(View.VISIBLE, View.GONE);
                                    progress.hide();
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setVisibility(View.GONE, View.VISIBLE);
                                    progress.hide();
                                }
                            });
                        }
                    }

                }
        );
    }

    private void setVisibility(int activeControls, int inactiveControls) {

        url.setVisibility(activeControls);
        webBtn.setVisibility(activeControls);
        mobileBtn.setVisibility(activeControls);

        loginRow.setVisibility(inactiveControls);
        passRow.setVisibility(inactiveControls);
        activateBtn.setVisibility(inactiveControls);
    }

    private void finishSetupAsync(final Device device, final String login, final String pass) {

        progress.setMessage("Activating ...");
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Result<String> result = OwncloudManager.finishSetup(device, login, pass);
                if (result.hasError())
                    showError(result.getError());

                Result<SshResult> portResult = InsiderManager.addPort(device, OwncloudManager.OWNCLOUD_PORT);
                if (portResult.hasError()) {
                    showError(portResult.getError());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            status();
                            progress.hide();
                        }
                    });
                }

            }
        });

    }

    public void showWeb(View view) {
        CharSequence text = url.getText();
        if (text != null && !text.toString().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text.toString()));
            startActivity(browserIntent);
        } else {
            progress.setMessage("url is not set");
            progress.show();
            progress.setCancelable(true);
        }
    }

    public void showMobile(View view) {
        CharSequence text = url.getText();
        if (text != null && !text.toString().isEmpty()) {
            Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.owncloud.android");
            startActivity(LaunchIntent);
        } else {
            progress.setMessage("url is not set");
            progress.show();
            progress.setCancelable(true);
        }
    }
}
