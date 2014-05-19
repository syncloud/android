package org.syncloud.android.app;

import android.app.Activity;
import android.app.ProgressDialog;
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

import org.syncloud.android.Params;
import org.syncloud.android.R;
import org.syncloud.android.activation.OwncloudManager;
import org.syncloud.app.InsiderManager;
import org.syncloud.model.PortMapping;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;


public class Owncloud extends Activity {

    private String device;
    private ProgressDialog progress;
    private TextView owncloudUrl;
    private LinearLayout logibRow;
    private LinearLayout passRow;
    private Button activateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owncloud);

        progress = new ProgressDialog(this);
        device = getIntent().getExtras().getString(Params.DEVICE_ADDRESS);

        owncloudUrl = (TextView) findViewById(R.id.owncloud_url);
        logibRow = (LinearLayout) findViewById(R.id.owncloud_login_row);
        passRow = (LinearLayout) findViewById(R.id.owncloud_pass_row);
        activateBtn = (Button) findViewById(R.id.owncloud_activate);


        status(device);
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

    private void status(final String device) {
        progress.setMessage("Checking ownCloud status ...");
        progress.show();

        AsyncTask.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        Result<Optional<PortMapping>> posrResult = InsiderManager
                                .localPortMapping(device, OwncloudManager.OWNCLOUD_PORT);

                        if (posrResult.hasError()) {
                            showError(posrResult.getError());
                            return;
                        }

                        if (posrResult.getValue().isPresent()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    owncloudUrl.setText(OwncloudManager.url(device));
                                    activated(true);
                                    progress.hide();
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activated(false);
                                    progress.hide();
                                }
                            });
                        }
                    }

                }
        );
    }

    private void activated(boolean active) {

        owncloudUrl.setVisibility(active ? View.VISIBLE : View.GONE);
        logibRow.setVisibility(active ? View.GONE : View.VISIBLE);
        passRow.setVisibility(active ? View.GONE : View.VISIBLE);
        activateBtn.setVisibility(active ? View.GONE : View.VISIBLE);
    }

    private void finishSetupAsync(final String device, final String login, final String pass) {

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
                            status(device);
                            progress.hide();
                        }
                    });
                }

            }
        });

    }
}
