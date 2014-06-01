package org.syncloud.android.activity.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import org.syncloud.model.Device;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;


public class Owncloud extends Activity {

    public static final String COM_OWNCLOUD_ANDROID = "com.owncloud.android";
    private Device device;
    private ProgressDialog progress;
    private TextView url;
    private LinearLayout loginRow;
    private LinearLayout passRow;
    private Button activateBtn;
    private Button webBtn;
    private Button mobileBtn;
    private LinearLayout activatedControls;
    private LinearLayout notActivatedControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_owncloud);

        progress = new ProgressDialog(this);
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);

        activatedControls = (LinearLayout) findViewById(R.id.owncloud_activated_controls);
        notActivatedControls = (LinearLayout) findViewById(R.id.owncloud_not_activated_controls);
        url = (TextView) findViewById(R.id.owncloud_url);
        loginRow = (LinearLayout) findViewById(R.id.owncloud_login_row);
        passRow = (LinearLayout) findViewById(R.id.owncloud_pass_row);
        activateBtn = (Button) findViewById(R.id.owncloud_activate);

        webBtn = (Button) findViewById(R.id.owncloud_web_btn);
        mobileBtn = (Button) findViewById(R.id.owncloud_mobile_btn);

        setVisibility(View.GONE, View.GONE);

        progress.setMessage("Checking ownCloud status ...");
        progress.setCancelable(false);
        progress.show();

        status();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.owncloud, menu);
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

        final String login = loginText.getText().toString();
        final String pass = passText.getText().toString();


        progress.setMessage("Activating ...");
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Result<SshResult> result = OwncloudManager.finishSetup(device, login, pass);
                if (result.hasError()) {
                    showError(result.getError());
                    return;
                }
                if (!result.getValue().ok()){
                    showError(result.getValue().getMessage());
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status();
                    }
                });

            }
        });

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

        AsyncTask.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        final Result<Optional<String>> result = OwncloudManager.owncloudUrl(device);

                        if (result.hasError()) {
                            showError("Unable to read status:\n" + result.getError());
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
        activatedControls.setVisibility(activeControls);
        notActivatedControls.setVisibility(inactiveControls);
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
            if (isInstalled(COM_OWNCLOUD_ANDROID)) {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(COM_OWNCLOUD_ANDROID);
                startActivity(LaunchIntent);
            } else {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                marketIntent.setData(Uri.parse("market://details?id=" + COM_OWNCLOUD_ANDROID));
                startActivity(marketIntent);
            }
        } else {
            progress.setMessage("url is not set");
            progress.show();
            progress.setCancelable(true);
        }
    }

    public boolean isInstalled(String app)
    {
        try{
            getPackageManager().getApplicationInfo(app, 0);
            return true;
        } catch( PackageManager.NameNotFoundException e ){
            return false;
        }
    }

    public void copyUrl(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("url", url.getText());
        clipboard.setPrimaryClip(clip);
    }
}
