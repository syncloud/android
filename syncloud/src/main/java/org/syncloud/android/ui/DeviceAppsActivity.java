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


import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DeviceAppsAdapter;
import org.syncloud.android.db.Db;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Commands;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.apps.sam.App;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.android.SyncloudApplication.appRegistry;

public class DeviceAppsActivity extends Activity {

    private Device device;
    private Db db;
    private boolean connected = false;
    private boolean showAdminApps = false;
    private Sam sam;
    private CommunicationDialog progress;
    private Ssh ssh;
    private InsiderManager insider;
    private Preferences preferences;


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

        SyncloudApplication application = (SyncloudApplication) getApplication();

        db = application.getDb();
        preferences = application.getPreferences();

        ssh = application.createSsh();
        sam = new Sam(ssh);

        insider = new InsiderManager(ssh);

        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);

        progress = new CommunicationDialog(this);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (!connected)
                    finish();
            }
        });

        txtDeviceTitle.setText(device.id().title);
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
                                ssh.execute(device, "reboot");
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

    private void onAppsLoaded(List<AppVersions> appsVersions) {
        connected = true;
        deviceAppsAdapter.clear();
        for (AppVersions app : appsVersions) {
            if (showAdminApps || app.app.appType() == App.Type.user)
                deviceAppsAdapter.add(app);
        }
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
        if (id == R.id.action_reboot_device) {
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
        new ProgressAsyncTask<Void, Result.Void>()
                .setTitle("Deactivating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, Result.Void>() {
                    @Override
                    public Result<Result.Void> run(Void... args) {
                        return insider.dropDomain(device).flatMap(new Result.Function<String,Result<Result.Void>>() {
                            @Override
                            public Result<Result.Void> apply(String input) throws Exception {
                                db.remove(device);
                                return Result.VOID;
                            }
                        });
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<Result.Void>() {
                    @Override
                    public void run(Result.Void result) {
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
