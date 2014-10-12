package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.adapters.DeviceAppsAdapter;
import org.syncloud.android.db.Db;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Command;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.apps.sam.App;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static java.util.Arrays.asList;
import static org.syncloud.android.SyncloudApplication.appRegistry;

public class DeviceAppsActivity extends Activity {

    private ProgressDialog progress;
    private DeviceAppsAdapter deviceAppsAdapter;
    private Device device;
    private Db db;
    private TextView deviceName;
    private boolean connected = false;
    private boolean showAdminApps = false;
    private Sam sam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_apps);

        progress = new ProgressDialog(this);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (!connected)
                    finish();
            }
        });

        deviceName = (TextView) findViewById(R.id.device_name);
        ImageButton nameEditBtn = (ImageButton) findViewById(R.id.device_name_edit_btn);
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);
        db = ((SyncloudApplication) getApplication()).getDb();
        deviceName.setText(device.getDisplayName());
        nameEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNameChange();
            }
        });
        final ListView listview = (ListView) findViewById(R.id.app_list);
        deviceAppsAdapter = new DeviceAppsAdapter(this);
        listview.setAdapter(deviceAppsAdapter);
        sam = new Sam(new Ssh());
        startProgress("Connecting to the device");
        execute(new Runnable() {
                    @Override
                    public void run() {
                        listApps();
                    }
                }

        );

    }

    private void showNameChange() {
        final EditText input = new EditText(this);
        input.setText(device.getDisplayName());
        new AlertDialog.Builder(this)
                .setTitle("Name change")
                .setMessage("Enter name for the device")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final Editable name = input.getText();
                        device.setName(name.toString());
                        deviceName.setText(device.getDisplayName());
                        execute(new Runnable() {
                            @Override
                            public void run() {
                                db.update(device);
                            }
                        });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private void startProgress(String message) {
        progress.setMessage(message);
        progress.setCancelable(false);
        progress.show();
    }

    private void listApps() {
        execute( new Runnable() {
                    @Override
                    public void run() {
                        progressUpdate("Refreshing app list");
                        final Result<List<AppVersions>> appsResult = sam.list(device);
                        if (!appsResult.hasError()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deviceAppsAdapter.clear();
                                    for (AppVersions app : appsResult.getValue()) {
                                        if (showAdminApps || app.app.appType() == App.Type.user)
                                            deviceAppsAdapter.add(app);
                                    }
                                }
                            });
                            progressDone();
                            connected = true;
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

    private void progressUpdate(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(message);
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
        if (id == R.id.action_update_apps) {
            updateSpm();
        } else if (id == R.id.action_show_admin_apps) {
            startProgress("Changing apps filter");
            item.setChecked(!item.isChecked());
            showAdminApps = item.isChecked();
            listApps();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSpm() {
        startProgress("Updating app list");
        execute(new Runnable() {
                    @Override
                    public void run() {
                        final Result<String> result = sam.updateSpm(device);
                        if (result.hasError()) {
                            progressError(result.getError());
                            return;
                        }

                        listApps();
                    }
                }
        );
    }

    public void run(final Command command, final String app) {
        startProgress("Running " + command + " for " + app);
        execute(new Runnable() {
            @Override
            public void run() {
                final Result<String> result = sam.run(device, command, app);
                if (result.hasError()) {
                    progressError(result.getError());
                    return;
                }

                listApps();

            }
        });
    }

    public void openApp(String appId) {
        if (appRegistry.containsKey(appId)) {
            Intent intent = new Intent(this, appRegistry.get(appId));
            intent.putExtra(SyncloudApplication.DEVICE, device);
            startActivity(intent);
        }
    }


}
