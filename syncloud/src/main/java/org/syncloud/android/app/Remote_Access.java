package org.syncloud.android.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.common.base.Optional;

import org.syncloud.android.Params;
import org.syncloud.android.R;
import org.syncloud.android.db.SavedDevice;
import org.syncloud.app.InsiderManager;
import org.syncloud.model.InsiderConfig;
import org.syncloud.model.InsiderDnsConfig;
import org.syncloud.ssh.Scp;
import org.syncloud.ssh.Ssh;
import org.syncloud.model.Result;
import org.syncloud.model.PortMapping;
import org.syncloud.model.SshResult;

import static android.os.AsyncTask.execute;
import static java.util.Arrays.asList;

public class Remote_Access extends Activity {

    public static final int REMOTE_ACCESS_PORT = 1022;
    private ProgressDialog progress;
    private Switch remoteAccessSwitch;
    private SavedDevice savedDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_access);
        progress = new ProgressDialog(this);
        savedDevice = new SavedDevice(this);
        progress.setMessage("Talking to the device");
        final String address = getIntent().getExtras().getString(Params.DEVICE_ADDRESS);
        remoteAccessSwitch = (Switch) findViewById(R.id.remote_access);
        remoteAccessSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                if (on) {
                    enable(address);
                } else {
                    disable(address);
                }
            }
        });
        status(address);

    }

    private void enable(final String address) {

        progress.setMessage("Enabling");
        progress.show();

        execute(new Runnable() {
            @Override
            public void run() {
                Result<SshResult> result = InsiderManager.addPort(address, REMOTE_ACCESS_PORT);
//
                if (result.hasError()) {
                    showError(result.getError());
                    return;
                }

                Result<SshResult> execute = Ssh.execute(address, asList(
                        "mkdir -p /root/.ssh",
                        "rm -rf /root/.ssh/id_dsa_syncloud_master*",
                        "ssh-keygen -b 1024 -t dsa -f /root/.ssh/id_dsa_syncloud_master -N ''",
                        "cat /root/.ssh/id_dsa_syncloud_master.pub > /root/.ssh/authorized_keys"));

                if (execute.hasError()) {
                    showError(execute.getError());
                    return;
                }

                Result<String> key = Scp.getFile(address, "/root/.ssh/id_dsa_syncloud_master");
                if (key.hasError()) {
                    showError(key.getError());
                    return;
                }

                Result<Optional<PortMapping>> localPortMapping = InsiderManager.localPortMapping(address, REMOTE_ACCESS_PORT);
                if (localPortMapping.hasError()) {
                    showError(localPortMapping.getError());
                    return;
                }

                Optional<PortMapping> value = localPortMapping.getValue();
                if (!value.isPresent()) {
                    showError("unable to get external port");
                    return;
                }

                Result<Optional<InsiderDnsConfig>> dnsResult = InsiderManager.dnsConfig(address);
                if (dnsResult.hasError()) {
                    showError(dnsResult.getError());
                    return;
                }

                Result<InsiderConfig> configResult = InsiderManager.config(address);
                if (configResult.hasError()) {
                    showError(configResult.getError());
                    return;
                }

                Optional<InsiderDnsConfig> dns = dnsResult.getValue();
                String host;
                if (dns.isPresent())
                    host = "device." + dns.get().getUser_domain() + "." + configResult.getValue().getDomain();
                else
                    host = address;

                savedDevice.insert(host, value.get().getExternal_port(), key.getValue());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status(address);
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

    private void status(final String address) {

        progress.setMessage("Checking status");
        progress.show();

        execute(new Runnable() {
            @Override
            public void run() {

                final Result<Optional<PortMapping>> result = InsiderManager
                        .localPortMapping(address, REMOTE_ACCESS_PORT);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean checked = !result.hasError() && result.getValue().isPresent();
                        remoteAccessSwitch.setChecked(checked);
                    }
                });

                if (result.hasError()) {
                    showError(result.getError());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.hide();
                        }
                    });
                }
            }
        });
    }

    private void disable(final String address) {

        progress.show();
        progress.setMessage("Disabling");

        execute(new Runnable() {
            @Override
            public void run() {

                final Result<SshResult> result = InsiderManager.removePort(address, REMOTE_ACCESS_PORT);

                if (result.hasError()) {
                    showError(result.getError());
                    return;
                }

                savedDevice.remove(address);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status(address);
                    }
                });

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device_settings, menu);
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
}
