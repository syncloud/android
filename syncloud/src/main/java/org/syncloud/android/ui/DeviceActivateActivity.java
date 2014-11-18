package org.syncloud.android.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Command;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.remote.RemoteAccessManager;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Endpoint;

import java.util.List;

import static org.syncloud.ssh.model.Credentials.getStandardCredentials;


public class DeviceActivateActivity extends Activity {

    //    private Function<String, String> progressFunction;
    private Endpoint endpoint;
    private boolean dnsReady = false;
    private Preferences preferences;
    private Device device;

    private CommunicationDialog progress;
    private TextView url;
    private LinearLayout domainSettings;
    private Button deactivateButton;
    private Sam sam;
    private InsiderManager insider;
    private RemoteAccessManager accessManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);

        endpoint = (Endpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        device = new Device(null, null, endpoint, getStandardCredentials());

        preferences = ((SyncloudApplication) getApplication()).getPreferences();

        progress = new CommunicationDialog(this);
        Ssh ssh = new Ssh(progress);
        sam = new Sam(ssh, progress);
        insider = new InsiderManager(ssh);
        accessManager = new RemoteAccessManager(insider, ssh);
        url = (TextView) findViewById(R.id.device_url);
        deactivateButton = (Button) findViewById(R.id.name_deactivate);
        domainSettings = (LinearLayout) findViewById(R.id.domain_settings);

        status();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the menu; this adds items to the action bar if it is present.
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

    private void status() {

        progress.start();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        url.setText("[user]." + preferences.getDomain());
                    }
                });

                final Result<String> domain_name = insider.userDomain(device);
                if (domain_name.hasError()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dnsReady = false;
                            progress.hide();
                            domainSettings.setVisibility(View.VISIBLE);
                            deactivateButton.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dnsReady = true;
                            domainSettings.setVisibility(View.GONE);
                            deactivateButton.setVisibility(View.VISIBLE);
                            url.setText(domain_name.getValue()+"."+preferences.getDomain());
                            progress.hide();
                        }
                    });

                }

            }
        });
    }

    public void deactivate(View view) {

        progress.start();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                progress.title("Deactivating device");
                final Result<String> redirectResult = insider.dropDomain(device);
                if (redirectResult.hasError()) {
                    progress.error(redirectResult.getError());
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

        progress.start();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Result<List<AppVersions>> updateResult = sam.update(device);
                if (updateResult.hasError()) {
                    return;
                }

                if (!updateResult.getValue().isEmpty()) {
                    if (sam.run(device, Command.Upgrade_All).hasError()) {
                        return;
                    }
                }

                if (!dnsReady) {

                    EditText userDomainText = (EditText) findViewById(R.id.user_domain);

                    final String email = preferences.getEmail();
                    final String pass = preferences.getPassword();
                    final String domain = userDomainText.getText().toString();

                    TextView status = (TextView) findViewById(R.id.dns_status);
                    boolean valid = true;

                    if (domain.matches("")) {
                        status.setText("enter domain");
                        valid = false;
                    }

                    if (!valid) {
                        progress.error("fix errors");
                        return;
                    }

                    progress.title("Setting redirect info");
                    final Result<String> redirectResult = insider.setRedirectInfo(device, preferences.getDomain(), preferences.getApiUrl());
                    if (redirectResult.hasError()) {
                        progress.error(redirectResult.getError());
                        return;
                    }

                    progress.title("Acquiring domain");
                    final Result<String> result = insider.acquireDomain(device, email, pass, domain);
                    if (result.hasError()) {
                        progress.error(result.getError());
                        return;
                    }
                }

                progress.title("Activating remote access");
                final Result<Device> remote = accessManager.enable(device, preferences.getDomain());
                if (remote.hasError()) {
                    progress.error(remote.getError());
                    return;
                }

                progress.title("Saving device");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

}
