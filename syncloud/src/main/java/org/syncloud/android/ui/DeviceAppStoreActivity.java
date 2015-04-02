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
import org.syncloud.platform.sam.AppVersions;
import org.syncloud.platform.sam.Sam;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.DomainModel;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.platform.sam.Commands.upgrade_all;

import static org.syncloud.platform.ssh.SshRunner.cmd;


public class DeviceAppStoreActivity extends Activity {

    private DeviceAppStoreAppsAdapter deviceAppsAdapter;
    private DomainModel domain;
    private boolean connected = false;
    private Sam sam;
    private CommunicationDialog progress;
    private Preferences preferences;
    private SshRunner ssh;
    private ConnectionPointProvider connectionPoint;


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

        ssh = new SshRunner();

        domain = (DomainModel) getIntent().getSerializableExtra(SyncloudApplication.DOMAIN);
        connectionPoint = application.connectionPoint(domain.device());

        final ListView listview = (ListView) findViewById(R.id.app_list);
        deviceAppsAdapter = new DeviceAppStoreAppsAdapter(this);
        listview.setAdapter(deviceAppsAdapter);
        sam = new Sam(new SshRunner(), preferences);

        listApps();
    }

    private void listApps() {
        new ProgressAsyncTask<Void, List<AppVersions>>()
                .setTitle("Refreshing app list")
                .setErrorMessage("Unable to get list of apps")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public List<AppVersions> run(Void... args) {
                        return sam.list(connectionPoint);
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
                .setErrorMessage("Unable to update sam")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public List<AppVersions> run(Void... args) {
                        return sam.update(connectionPoint);
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
                .setErrorMessage("Unable to ugrade apps")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public List<AppVersions> run(Void... args) {
                        sam.run(connectionPoint, upgrade_all);
                        return sam.list(connectionPoint);
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
                .setErrorMessage("Command execution failed")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<String, List<AppVersions>>() {
                    @Override
                    public List<AppVersions> run(String... args) {
                        sam.run(connectionPoint, args);
                        return sam.list(connectionPoint);
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
                                ssh.run(connectionPoint, cmd("reboot"));
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
