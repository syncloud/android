package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.common.base.Optional;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DeviceAppStoreAppsAdapter;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Sam;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.apps.sam.Commands.upgrade_all;

public class DeviceAppStoreActivity extends Activity {

    private DeviceAppStoreAppsAdapter deviceAppsAdapter;
    private Device device;
    private boolean connected = false;
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
        sam = new Sam(ssh, preferences);

        listApps();
    }

    private void listApps() {
        new ProgressAsyncTask<Void, List<AppVersions>>()
                .setTitle("Refreshing app list")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public AsyncResult<List<AppVersions>> run(Void... args) {
                        return new AsyncResult<List<AppVersions>>(
                                sam.list(device),
                                "unable to get list of apps");
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
                    public AsyncResult<List<AppVersions>> run(Void... args) {
                        return new AsyncResult<List<AppVersions>>(
                                sam.update(device),
                                "unable to update sam");
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
                    public AsyncResult<List<AppVersions>> run(Void... args) {
                        if (!sam.run(device, upgrade_all)) {
                            return new AsyncResult<List<AppVersions>>(
                                    Optional.<List<AppVersions>>absent(),
                                    "unable to upgrade apps");
                        } else {
                            return new AsyncResult<List<AppVersions>>(
                                    sam.list(device),
                                    "unable to get list of apps");
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
                    public AsyncResult<List<AppVersions>> run(String... args) {
                        if (!sam.run(device, args)) {
                            return new AsyncResult<List<AppVersions>>(
                                    Optional.<List<AppVersions>>absent(),
                                    "unable to execute command");
                        } else {
                            return new AsyncResult<List<AppVersions>>(
                                    sam.list(device),
                                    "unable to get list of apps");
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
                .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listApps();
                    }
                })
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
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        } else if (id == R.id.action_update_apps) {
            updateSam();
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
