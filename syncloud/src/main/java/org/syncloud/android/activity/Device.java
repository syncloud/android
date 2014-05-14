package org.syncloud.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.syncloud.android.AppsAdapter;
import org.syncloud.android.R;
import org.syncloud.model.App;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.ssh.Spm;

import java.util.List;


public class Device extends Activity {

    private String address;
    private ProgressDialog progress;
    private AppsAdapter appsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        progress = new ProgressDialog(this);

        TextView deviceAddress = (TextView) findViewById(R.id.device_address);
        address = getIntent().getExtras().getString("address");
        deviceAddress.setText(address);

        final ListView listview = (ListView) findViewById(R.id.app_list);
        appsAdapter = new AppsAdapter(this);
        listview.setAdapter(appsAdapter);

        checkSystem();

        listview.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        App app = (App) parent.getAdapter().getItem(position);
                        Intent intent = new Intent(Device.this, AppDetails.class);
                        intent.putExtra("device_address", address);
                        intent.putExtra("app_name", app.getName());
                        intent.putExtra("app", app.getId());
                        startActivity(intent);
                    }
                }
        );
    }

    private void checkSystem() {
        progress.setMessage("Checking system");
        progress.show();
        AsyncTask.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        final Result<SshResult> result = Spm.ensureSpmInstalled(address);
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
                        final Result<List<App>> appsResult = Spm.list(address);
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
                progress.setMessage(message);
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
            Intent intent = new Intent(Device.this, DeviceSettings.class);
            intent.putExtra("device_address", address);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_reinstall_spm) {
            Spm.installedSpm(address);
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
                final Result<SshResult> result = Spm.run(action, address, app);
                if (result.hasError()) {
                    progressError(result.getError());
                    return;
                }

                SshResult sshResult = result.getValue();
                if (!sshResult.ok()){
                    progressError(sshResult.getMessage());
                    return;
                }

                listApps();

            }
        });
    }

    /*public void dns(View view) {
        Intent intent = new Intent(this, DnsActivity.class);
        intent.putExtra("address", address);
        startActivity(intent);
    }

    public void owncloud(View view) {
        Intent intent = new Intent(this, OwncloudActivity.class);
        intent.putExtra("address", address);
        startActivity(intent);
    }*/

    /*public void install(View view) {

        new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                Result<String> result = Spm.run(Spm.Commnand.Install, strings[0], strings[1]);
                return result.hasError() ? result.getError() : result.getValue();
            }

            @Override
            protected void onPostExecute(String status) {
                executeStatus.setText(status);
                status(true);
            }
        }.execute(address, app);
    }*/
}
