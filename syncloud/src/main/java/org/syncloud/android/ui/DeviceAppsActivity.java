package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.View;


import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.adapters.DeviceAppsAdapter;
import org.syncloud.android.db.Db;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Command;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.apps.sam.App;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.android.SyncloudApplication.appRegistry;

public class DeviceAppsActivity extends Activity {

    private DeviceAppsAdapter deviceAppsAdapter;
    private Device device;
    private Db db;
    private TextView deviceName;
    private boolean connected = false;
    private boolean showAdminApps = false;
    private Sam sam;
    private CommunicationDialog progress;
    private Ssh ssh;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_apps);

        progress = new CommunicationDialog(this);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (!connected)
                    finish();
            }
        });

        SyncloudApplication application = (SyncloudApplication) getApplication();

        preferences = application.getPreferences();

        deviceName = (TextView) findViewById(R.id.device_name);
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);
        db = application.getDb();
        deviceName.setText(device.userDomain());

        final ListView listview = (ListView) findViewById(R.id.app_list);
        deviceAppsAdapter = new DeviceAppsAdapter(this);
        listview.setAdapter(deviceAppsAdapter);
        ssh = application.createSsh(progress);
        sam = new Sam(ssh, progress);
        progress.start();
        execute(new Runnable() {
                    @Override
                    public void run() {
                        listApps();
                    }
                });

    }

    public void reboot() {
        new AlertDialog.Builder(this)
                .setTitle("Reboot")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        execute(new Runnable() {
                            @Override
                            public void run() {
                                ssh.execute(device, "reboot");
                            }
                        });
                    }
                })
                .show();
    }
    
    private void listApps() {
        execute(new Runnable() {
                    @Override
                    public void run() {
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
                            progress.stop();
                            connected = true;
                        }
                    }
                }
        );
    }

    private void showUpgradeQuestion() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(DeviceAppsActivity.this)
                        .setTitle("Updates available")
                        .setPositiveButton("Update all apps", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!sam.run(device, Command.Upgrade_All).hasError()) {
                                            listApps();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.stop();
                                    }
                                });
                            }
                        })
                        .show();
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
            progress.start();
            progress.title("Changing apps filter");
            item.setChecked(!item.isChecked());
            showAdminApps = item.isChecked();
            listApps();
        } else if (id == R.id.action_reboot_device) {
          reboot();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateSpm() {
        progress.start();
        execute(new Runnable() {
                    @Override
                    public void run() {
                        Result<List<AppVersions>> updatesResult = sam.update(device);
                        if (updatesResult.hasError()) {
                            return;
                        }
                        if (!updatesResult.getValue().isEmpty()) {
                            showUpgradeQuestion();
                        } else {
                            listApps();
                        }
                    }
                }
        );
    }

    public void run(final Command command, final String app) {
        progress.start();
        execute(new Runnable() {
            @Override
            public void run() {
                final Result<String> result = sam.run(device, command, app);
                if (result.hasError()) {
                    progress.error(result.getError());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }


}
