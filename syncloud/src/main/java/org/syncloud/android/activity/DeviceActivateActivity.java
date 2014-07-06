package org.syncloud.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.app.InsiderManager;
import org.syncloud.app.RemoteAccessManager;
import org.syncloud.model.Device;
import org.syncloud.model.InsiderConfig;
import org.syncloud.model.InsiderDnsConfig;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.model.User;
import org.syncloud.redirect.UserService;
import org.syncloud.ssh.Spm;


public class DeviceActivateActivity extends Activity {

    private Function<String, String> progressFunction;
    private Device device;
    private ProgressDialog progress;
    private TextView managedDomain;
    private TextView userDomainName;
    private boolean dnsReady = false;
    private LinearLayout dnsControl;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);
        userService = ((SyncloudApplication) getApplication()).getUserService();
        progress = new ProgressDialog(this);
        progressFunction = new Function<String, String>() {
            @Override
            public String apply(String input) {
                showProgress(input);
                return null;
            }

        };
        managedDomain = (TextView) findViewById(R.id.managed_domain);
        userDomainName = (TextView) findViewById(R.id.user_domain_name);
        dnsControl = (LinearLayout) findViewById(R.id.dns_control);

        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);

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
                progress.setMessage(message);
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

        progress.setMessage("Checking device status ...");
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Result<InsiderConfig> config = InsiderManager.config(device);

                if (config.hasError()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.hide();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            managedDomain.setText(config.getValue().getDomain());
                        }
                    });

                    final Result<Optional<InsiderDnsConfig>> dnsConfig = InsiderManager.dnsConfig(device);
                    if (dnsConfig.hasError()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.hide();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Optional<InsiderDnsConfig> dnsConfigs = dnsConfig.getValue();
                                if (dnsConfigs.isPresent()) {
                                    dnsReady = true;
                                    dnsControl.setVisibility(View.GONE);
                                    userDomainName.setText(dnsConfigs.get().getUser_domain());
                                } else {
                                    userDomainName.setText("");
                                }
                                progress.hide();
                            }
                        });

                    }
                }
            }
        });
    }

    public void activate(View view) {

        progress.setMessage("Connecting to the device");
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                showProgress("Installing system tools");

                Result<Boolean> systemTools = Spm.ensureAdminToolsInstalled(device, progressFunction);
                if (systemTools.hasError()) {
                    showError(systemTools.getError());
                    return;
                }

                if (!dnsReady) {

                    EditText emailText = (EditText) findViewById(R.id.name_email);
                    EditText passText = (EditText) findViewById(R.id.name_pass);
                    EditText userDomainText = (EditText) findViewById(R.id.user_domain);

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

                    if (!valid) {
                        showError("fix errors");
                        return;
                    }

                    showProgress("Activating public name");

                    final Result<SshResult> result;
                    if (domain.matches("")) {
                        Result<User> user = userService.getUser(email, pass);
                        if (user.hasError()) {
                            showError(user.getError());
                            return;
                        }
                    } else {
                        userService.getOrCreate(email, pass, domain);
                    }

                    result = InsiderManager.activateExistingName(device, email, pass);

                    if (result.hasError()) {
                        showError(result.getError());
                        return;
                    }
                }

                showProgress("Activating remote access");

                final Result<Device> remoteDeviceResult = RemoteAccessManager.enable(device);
                if (remoteDeviceResult.hasError()) {
                    showError(remoteDeviceResult.getError());
                    return;
                }

                showProgress("Saving device");

                final Db db = ((SyncloudApplication) getApplication()).getDb();
                db.insert(remoteDeviceResult.getValue());

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
