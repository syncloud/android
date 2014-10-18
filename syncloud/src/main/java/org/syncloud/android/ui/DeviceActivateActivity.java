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
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.remote.RemoteAccessManager;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;


public class DeviceActivateActivity extends Activity {

    //    private Function<String, String> progressFunction;
    private DirectEndpoint endpoint;
    private boolean dnsReady = false;
    private Preferences preferences;
    private Device discoveredDevice;

    private CommunicationDialog progress;
    private TextView url;
    private LinearLayout domainSettings;
    private Button deactivateButton;
    private Sam sam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);

        endpoint = (DirectEndpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        discoveredDevice = new Device(null, null, null, endpoint);

        preferences = ((SyncloudApplication) getApplication()).getPreferences();

        progress = new CommunicationDialog(this);
        sam = new Sam(new Ssh());
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

                final Result<String> domain_name = InsiderManager.userDomain(discoveredDevice);
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
                final Result<String> redirectResult = InsiderManager.dropDomain(discoveredDevice);
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

                if (!sam.update(discoveredDevice, progress)) {
                    return;
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
                    final Result<String> redirectResult = InsiderManager.setRedirectInfo(discoveredDevice, preferences.getDomain(), preferences.getApiUrl());
                    if (redirectResult.hasError()) {
                        progress.error(redirectResult.getError());
                        return;
                    }

                    progress.title("Acquiring domain");
                    final Result<String> result = InsiderManager.acquireDomain(discoveredDevice, email, pass, domain);
                    if (result.hasError()) {
                        progress.error(result.getError());
                        return;
                    }
                }

                progress.title("Activating remote access");
                final Result<Device> remote = RemoteAccessManager.enable(discoveredDevice, preferences.getDomain());
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
