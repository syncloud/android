package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DeviceAppsAdapter;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Sam;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.android.SyncloudApplication.appRegistry;

public class DeviceAppsActivity extends Activity {

    private static Logger logger = Logger.getLogger(DeviceAppsActivity.class);

    private SyncloudApplication application;
    private Device device;
    private Sam sam;
    private CommunicationDialog progress;
    private InsiderManager insider;
    private Preferences preferences;
    private SshRunner ssh;
    private ConnectionPointProvider connectionPoint;

    private TextView txtDeviceTitle;
    private TextView txtDomainName;
    private ListView listApplications;

    private DeviceAppsAdapter deviceAppsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_apps);

        txtDeviceTitle = (TextView) findViewById(R.id.txt_device_title);
        txtDomainName = (TextView) findViewById(R.id.txt_domain_name);

        application = (SyncloudApplication) getApplication();

        preferences = application.getPreferences();

        ssh = new SshRunner();
        sam = new Sam(new SshRunner(), preferences);
        insider = new InsiderManager();

        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);
        connectionPoint = application.connectionPoint(device);

        progress = new CommunicationDialog(this);

        txtDeviceTitle.setText(device.title());
        txtDomainName.setText(device.userDomain());

        listApplications = (ListView) findViewById(R.id.app_list);
        deviceAppsAdapter = new DeviceAppsAdapter(this);
        listApplications.setAdapter(deviceAppsAdapter);
        listApplications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object obj = listApplications.getItemAtPosition(position);
                AppVersions appVersions = (AppVersions)obj;
                openApp(appVersions.app.id);
            }
        });

        listApps();
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
                                ssh.run(connectionPoint, "reboot");
                            }
                        });
                    }
                })
                .show();
    }

    private void listApps() {
        new ProgressAsyncTask<Void, List<AppVersions>>()
                .setTitle("Refreshing app list")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public AsyncResult<List<AppVersions>> run(Void... args) {
                        return new AsyncResult<List<AppVersions>>(
                                sam.list(connectionPoint),
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

    private void onAppsLoaded(List<AppVersions> appsVersions) {
        deviceAppsAdapter.clear();
        for (AppVersions app : appsVersions) {
            if (app.app.ui && app.installed())
                deviceAppsAdapter.add(app);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        } else if (id == R.id.action_reboot_device) {
            reboot();
        } else if (id == R.id.action_deactivate) {
            deactivate();
        } else if (id == R.id.action_manage_apps) {
            Intent intent = new Intent(this, DeviceAppStoreActivity.class);
            intent.putExtra(SyncloudApplication.DEVICE, device);
            startActivityForResult(intent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    private void openApp(String appId) {
        if (appRegistry.containsKey(appId)) {
            Intent intent = new Intent(this, appRegistry.get(appId));
            intent.putExtra(SyncloudApplication.DEVICE, device);
            startActivity(intent);
        }
    }

    public void deactivate() {
        new ProgressAsyncTask<Void, Void>() {}
                .setTitle("Deactivating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, Void>() {
                    @Override
                    public AsyncResult<Void> run(Void... args) {
                        insider.dropDomain(connectionPoint);
//                        if (insider.dropDomain(device))
//                            db.remove(device);
                        return null;
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<Void>() {
                    @Override
                    public void run(Void result) {
                        finish();
                    }
                })
                .execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }
}
