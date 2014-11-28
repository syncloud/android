package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DeviceAppStoreAppsAdapter;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.App;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Commands;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.common.model.Result.error;

public class DeviceAppStoreActivity extends Activity {

    private DeviceAppStoreAppsAdapter deviceAppsAdapter;
    private Device device;
    private boolean connected = false;
    private boolean showAdminApps = false;
    private Sam sam;
    private CommunicationDialog progress;
    private Ssh ssh;
    private Preferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_app_store);

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

        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);

        final ListView listview = (ListView) findViewById(R.id.app_list);
        deviceAppsAdapter = new DeviceAppStoreAppsAdapter(this);
        listview.setAdapter(deviceAppsAdapter);
        ssh = application.createSsh();
        sam = new Sam(ssh);

        listApps();
    }

    private void listApps() {
        new ProgressAsyncTask<Void, List<AppVersions>>()
                .setTitle("Refreshing app list")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public Result<List<AppVersions>> run(Void... args) {
                        return sam.list(device);
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<List<AppVersions>>() {
                    @Override
                    public void run(List<AppVersions> appsVersions) {
                        onAppsLoaded(appsVersions);
                    }
                })
                .execute();
    }

    private void updateSam() {
        new ProgressAsyncTask<Void, List<AppVersions>>()
                .setTitle("Checking for updates")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public Result<List<AppVersions>> run(Void... args) {
                        return sam.update(device);
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<List<AppVersions>>() {
                    @Override
                    public void run(List<AppVersions> appsVersions) {
                        onSamUpdated(appsVersions);
                    }
                })
                .execute();

    }

    private void upgradeAll() {
        new ProgressAsyncTask<Void, List<AppVersions>>()
                .setTitle("Upgrading all apps")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public Result<List<AppVersions>> run(Void... args) {
                        Result<String> upgradeResult = sam.run(device, Commands.upgrade_all);
                        if (upgradeResult.hasError()) {
                            return error(upgradeResult.getError());
                        } else {
                            return sam.list(device);
                        }
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<List<AppVersions>>() {
                    @Override
                    public void run(List<AppVersions> appsVersions) {
                        onAppsLoaded(appsVersions);
                    }
                })
                .execute();
    }

    public void runSam(String... arguments) {
        new ProgressAsyncTask<String, List<AppVersions>>()
                .setTitle("Executing command")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<String, List<AppVersions>>() {
                    @Override
                    public Result run(String... args) {
                        Result<String> commandResult = sam.run(device, args);
                        if (commandResult.hasError()) {
                            return error(commandResult.getError());
                        } else {
                            return sam.list(device);
                        }
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<List<AppVersions>>() {
                    @Override
                    public void run(List<AppVersions> appsVersions) {
                        onAppsLoaded(appsVersions);
                    }
                })
                .execute(arguments);
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

    private void onAppsLoaded(List<AppVersions> appsVersions) {
        connected = true;
        deviceAppsAdapter.clear();
        for (AppVersions app : appsVersions) {
            if (showAdminApps || app.app.appType() == App.Type.user)
                deviceAppsAdapter.add(app);
        }
    }

    private void onSamUpdated(List<AppVersions> appsVersions) {
        if (appsVersions.isEmpty()) {
            listApps();
        } else {
            askUpgradeQuestion();
        }
    }

    private void askUpgradeQuestion() {
        new AlertDialog.Builder(DeviceAppStoreActivity.this)
                .setTitle("Updates available")
                .setPositiveButton("Update all apps", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        upgradeAll();
                    }
                })
                .setNegativeButton("Not now", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_store, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_update_apps) {
            updateSam();
        } else if (id == R.id.action_show_admin_apps) {
            item.setChecked(!item.isChecked());
            showAdminApps = item.isChecked();
            listApps();
        } else if (id == R.id.action_reboot_device) {
          reboot();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }
}
