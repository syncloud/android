package org.syncloud.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.android.adapter.AppsAdapter;
import org.syncloud.android.config.Params;
import org.syncloud.android.R;
import org.syncloud.model.App;
import org.syncloud.model.Device;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.ssh.Spm;

import java.util.List;

import static org.syncloud.android.config.AppRegistry.registry;


public class DeviceActivity extends Activity {

    private ProgressDialog progress;
    private AppsAdapter appsAdapter;
    private Device device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        progress = new ProgressDialog(this);

        TextView deviceAddress = (TextView) findViewById(R.id.device_address);
        device = (Device)getIntent().getSerializableExtra(Params.DEVICE);
        deviceAddress.setText(device.getHost());

        final ListView listview = (ListView) findViewById(R.id.app_list);
        appsAdapter = new AppsAdapter(this);
        listview.setAdapter(appsAdapter);

        checkSystem();
    }

    private void checkSystem() {
        progress.setMessage("Checking system");
        progress.show();
        AsyncTask.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final Result<SshResult> result = Spm.ensureSpmInstalled(device);
                        if (result.hasError()) {
                            progressError(result.getError());
                            return;
                        }

                        listApps();
                    }
                }
        );
    }

    private void listApps() {
        AsyncTask.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        progressUpdate("Refreshing app list");
                        final Result<List<App>> appsResult = Spm.list(device);
                        if (!appsResult.hasError()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    appsAdapter.clear();
                                    appsAdapter.addAll(appsResult.getValue());
                                }
                            });
                            progressDone();
                        } else {
                            progressError(appsResult.getError());
                        }
                    }
                }
        );
    }

    private void progressDone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.hide();
            }
        });
    }

    private void progressError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(StringUtils.right(message, 500));
                progress.setCancelable(true);
            }
        });
    }

    private void progressUpdate(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(message);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
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
        } else if (id == R.id.action_reinstall_spm) {

            progress.setMessage("Reinstalling package manager");
            progress.show();
            AsyncTask.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            final Result<SshResult> result = Spm.installSpm(device);
                            if (result.hasError()) {
                                progressError(result.getError());
                                return;
                            }

                            listApps();
                        }
                    }
            );

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void run(final Spm.Commnand action, final String app) {
        progress.setMessage("Running " + action.name().toLowerCase() + " for " + app);
        progress.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Result<SshResult> result = Spm.run(action, device, app);
                if (result.hasError()) {
                    progressError(result.getError());
                    return;
                }

                SshResult sshResult = result.getValue();
                if (!sshResult.ok()) {
                    progressError(sshResult.getMessage());
                    return;
                }

                listApps();

            }
        });
    }

    public void openApp(String appId) {
        if (registry.containsKey(appId)) {
            Intent intent = new Intent(this, registry.get(appId));
            intent.putExtra(Params.DEVICE, device);
            startActivity(intent);
        }
    }


}
