package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.android.ui.adapters.DeviceAppStoreAppsAdapter;
import org.syncloud.android.ui.adapters.DeviceAppsAdapter;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.App;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Command;
import org.syncloud.apps.sam.Commands;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.android.SyncloudApplication.appRegistry;
import static org.syncloud.common.model.Result.error;

public class DeviceAppStoreActivity extends Activity {

    private DeviceAppStoreAppsAdapter deviceAppsAdapter;
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

        deviceName = (TextView) findViewById(R.id.device_name);
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);
        db = application.getDb();
        deviceName.setText(device.userDomain());

        final ListView listview = (ListView) findViewById(R.id.app_list);
        deviceAppsAdapter = new DeviceAppStoreAppsAdapter(this);
        listview.setAdapter(deviceAppsAdapter);
        ssh = application.createSsh();
        sam = new Sam(ssh);

        new ListAppsTask().execute();
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
            new ListAppsTask().execute();
        } else {
            askUpgradeQuestion();
        }
    }

    private void askUpgradeQuestion() {
        new AlertDialog.Builder(DeviceAppStoreActivity.this)
                .setTitle("Updates available")
                .setPositiveButton("Update all apps", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new UpgradeAllTask().execute();
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
            new UpdateSamTask().execute();
        } else if (id == R.id.action_show_admin_apps) {
            item.setChecked(!item.isChecked());
            showAdminApps = item.isChecked();
            new ListAppsTask().execute();
        } else if (id == R.id.action_reboot_device) {
          reboot();
        }

        return super.onOptionsItemSelected(item);
    }

    public void runSam(String... args) {
        new SamTask().execute(args);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

    class UpdateSamTask extends AsyncTask<Void, Void, Result<List<AppVersions>>> {
        @Override
        protected void onPreExecute() {
            progress.start();
            progress.title("Checking for updates");
        }

        @Override
        protected Result<List<AppVersions>> doInBackground(Void... voids) {
            Result<List<AppVersions>> updatesResult = sam.update(device);
            return updatesResult;
        }

        @Override
        protected void onPostExecute(final Result<List<AppVersions>> result) {
            if (result.hasError()) {
                progress.error(result.getError());
            } else {
                progress.stop();
                onSamUpdated(result.getValue());
            }
        }
    }

    class UpgradeAllTask extends AsyncTask<Void, Void, Result<List<AppVersions>>> {
        @Override
        protected void onPreExecute() {
            progress.start();
            progress.title("Upgrading all apps");
        }

        @Override
        protected Result<List<AppVersions>> doInBackground(Void... voids) {
            Result<String> upgradeResult = sam.run(device, Command.Upgrade_All);
            if (upgradeResult.hasError()) {
                return error(upgradeResult.getError());
            } else {
                return sam.list(device);
            }
        }

        @Override
        protected void onPostExecute(final Result<List<AppVersions>> result) {
            if (result.hasError()) {
                progress.error(result.getError());
                onAppsLoaded(result.getValue());
            } else {
                progress.stop();
                onAppsLoaded(result.getValue());
            }
        }
    }

    class ListAppsTask extends AsyncTask<Void, Void, Result<List<AppVersions>>> {
        @Override
        protected void onPreExecute() {
            progress.start();
            progress.title("Refreshing app list");
        }

        @Override
        protected Result<List<AppVersions>> doInBackground(Void... voids) {
            return sam.list(device);
        }

        @Override
        protected void onPostExecute(final Result<List<AppVersions>> result) {
            if (result.hasError()) {
                progress.error(result.getError());
            } else {
                progress.stop();
                onAppsLoaded(result.getValue());
            }
        }
    }

    class SamTask extends AsyncTask<String, Void, Result<List<AppVersions>>> {
        @Override
        protected void onPreExecute() {
            progress.start();
            progress.title("Executing command");
        }

        @Override
        protected Result<List<AppVersions>> doInBackground(String... arguments) {
            Result<String> commandResult = sam.run(device, arguments);
            if (commandResult.hasError()) {
                return error(commandResult.getError());
            } else {
                return sam.list(device);
            }
        }

        @Override
        protected void onPostExecute(final Result<List<AppVersions>> result) {
            if (result.hasError()) {
                progress.error(result.getError());
            } else {
                progress.stop();
                onAppsLoaded(result.getValue());
            }
        }
    }
}
