package org.syncloud.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.common.model.Result;
import org.syncloud.insider.InsiderManager;
import org.syncloud.redirect.UserService;
import org.syncloud.remote.RemoteAccessManager;
import org.syncloud.spm.Spm;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;


public class DeviceActivateActivity extends Activity {

    private Function<String, String> progressFunction;
    private DirectEndpoint endpoint;
    private ProgressDialog progress;
    private TextView url;
    private boolean dnsReady = false;
    private LinearLayout dnsControl;
    private Preferences preferences;
    private Button deactivateButton;
    private Device discoveredDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);

        preferences = ((SyncloudApplication) getApplication()).getPreferences();

        progress = new ProgressDialog(this);
//        progress.setF
        progressFunction = new Function<String, String>() {
            @Override
            public String apply(String input) {
                showProgress(input);
                return null;
            }

        };
        url = (TextView) findViewById(R.id.device_url);
        deactivateButton = (Button) findViewById(R.id.name_deactivate);
        deactivateButton.setVisibility(View.GONE);

        dnsControl = (LinearLayout) findViewById(R.id.dns_control);
        endpoint = (DirectEndpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        discoveredDevice = new Device(null, null, null, endpoint);
        status();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.dns, menu);
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

    private void showProgress(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setTitle(message);
            }
        });
    }

    private void showError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(message);
                progress.setCancelable(true);
            }
        });
    }

    private void status() {

        progress.setTitle("Checking device status ...");
        progress.setMessage("");
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        url.setText("[user]." + preferences.getDomain());
                    }
                });

                final Result<String> fullName = InsiderManager.fullName(discoveredDevice);
                if (fullName.hasError()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dnsReady = false;
                            progress.hide();
                            dnsControl.setVisibility(View.VISIBLE);
                            deactivateButton.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dnsReady = true;
                            dnsControl.setVisibility(View.GONE);
                            deactivateButton.setVisibility(View.VISIBLE);
                            url.setText(fullName.getValue());
                            progress.hide();
                        }
                    });

                }

            }
        });
    }

    public void deactivate(View view) {

        progress.setTitle("Connecting to the device");
        progress.setMessage("");
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                showProgress("Checking system tools");
                Result<Boolean> systemTools = Spm.ensureAdminToolsInstalled(discoveredDevice, progressFunction);
                if (systemTools.hasError()) {
                    showError(systemTools.getError());
                    return;
                }

                showProgress("Deactivating device");
                final Result<String> redirectResult = InsiderManager.dropDomain(discoveredDevice);
                if (redirectResult.hasError()) {
                    showError(redirectResult.getError());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.hide();
                        status();
                    }
                });
            }
        });

    }

    public void activate(View view) {

        progress.setTitle("Connecting to the device");
        progress.setMessage("");
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                showProgress("Checking system tools");

                Result<Boolean> systemTools = Spm.ensureAdminToolsInstalled(discoveredDevice, progressFunction);
                if (systemTools.hasError()) {
                    showError(systemTools.getError());
                    return;
                }

                if (!dnsReady) {

                    EditText emailText = (EditText) findViewById(R.id.name_email);
                    EditText passText = (EditText) findViewById(R.id.name_pass);
                    EditText userDomainText = (EditText) findViewById(R.id.user_domain);
                    CheckBox existingUserCheck = (CheckBox) findViewById(R.id.existing_user_check);

                    final String email = emailText.getText().toString();
                    final String pass = passText.getText().toString();
                    final String domain = userDomainText.getText().toString();

                    TextView status = (TextView) findViewById(R.id.dns_status);
                    boolean valid = true;

                    if (email.matches("")) {
                        status.setText("enter email");
                        valid = false;
                    }

                    if (pass.matches("")) {
                        status.setText("enter password");
                        valid = false;
                    }

                    if (domain.matches("")) {
                        status.setText("enter domain");
                        valid = false;
                    }

                    if (!valid) {
                        showError("fix errors");
                        return;
                    }

                    showProgress("Setting redirect info");

                    final Result<String> redirectResult = InsiderManager.setRedirectInfo(discoveredDevice, preferences.getDomain(), preferences.getApiUrl());
                    if (redirectResult.hasError()) {
                        showError(redirectResult.getError());
                        return;
                    }

                    showProgress("Activating public name");
                    if (!existingUserCheck.isChecked()) {
                        Result<Boolean> user = UserService.getOrCreate(email, pass, domain, preferences.getApiUrl());
                        if (user.hasError()) {
                            showError(user.getError());
                            return;
                        }
                    }

                    showProgress("Acquiring domain");
                    final Result<String> result = InsiderManager.acquireDomain(discoveredDevice, email, pass, domain);
                    if (result.hasError()) {
                        showError(result.getError());
                        return;
                    }
                }

                showProgress("Activating remote access");
                final Result<Device> remote = RemoteAccessManager.enable(discoveredDevice, preferences.getDomain());
                if (remote.hasError()) {
                    showError(remote.getError());
                    return;
                }

                showProgress("Saving device");
                Db db = ((SyncloudApplication) getApplication()).getDb();
                db.insert(remote.getValue());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.hide();
                        finish();
                    }
                });

            }
        });

    }

}
