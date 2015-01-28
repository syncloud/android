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
import android.widget.Toast;


import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DeviceAppsAdapter;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Sam;
import org.syncloud.apps.server.Server;
import org.syncloud.redirect.RedirectService;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.ConnectionPoint;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.DomainModel;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Key;

import java.util.List;

import static android.os.AsyncTask.execute;
import static org.syncloud.android.SyncloudApplication.appRegistry;
import static org.syncloud.ssh.SimpleConnectionPointProvider.simple;
import static org.syncloud.ssh.model.Credentials.getStandardCredentials;

public class DeviceAppsActivity extends Activity {

    private static Logger logger = Logger.getLogger(DeviceActivateActivity.class);

    private SyncloudApplication application;
    private DomainModel domain;
    private Sam sam;
    private CommunicationDialog progress;
    private Preferences preferences;
    private SshRunner ssh;
    private ConnectionPointProvider connectionPoint;
    private RedirectService redirectService;
    private Server server;


    private TextView txtDeviceTitle;
    private TextView txtDomainName;
    private TextView txtAppsError;
    private ListView listApplications;

    private DeviceAppsAdapter deviceAppsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_apps);

        txtDeviceTitle = (TextView) findViewById(R.id.txt_bold_title);
        txtDomainName = (TextView) findViewById(R.id.txt_first_line);

        application = (SyncloudApplication) getApplication();

        preferences = application.getPreferences();

        ssh = new SshRunner();
        sam = new Sam(new SshRunner(), preferences);

        domain = (DomainModel) getIntent().getSerializableExtra(SyncloudApplication.DOMAIN);
        connectionPoint = application.connectionPoint(domain.device());
        redirectService = application.redirectService();

        server = new Server(ssh);

        progress = new CommunicationDialog(this);

        txtDomainName.setText(domain.userDomain());
        txtDeviceTitle.setText(domain.device().id().title());

        listApplications = (ListView) findViewById(R.id.list_apps);
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

        txtAppsError = (TextView) findViewById(R.id.txt_apps_error);

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
                .showError(false)
                .doWork(new ProgressAsyncTask.Work<Void, List<AppVersions>>() {
                    @Override
                    public AsyncResult<List<AppVersions>> run(Void... args) {
                        return new AsyncResult<List<AppVersions>>(
                                sam.list(connectionPoint),
                                "unable to get list of apps");
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<List<AppVersions>>() {
                    @Override
                    public void run(AsyncResult<List<AppVersions>> result) {
                        onAppsLoaded(result);
                    }
                })
                .execute();
    }

    private void onAppsLoaded(AsyncResult<List<AppVersions>> result) {
        if (result.hasError()) {
            listApplications.setVisibility(View.GONE);
            txtAppsError.setVisibility(View.VISIBLE);
            txtAppsError.setText(result.getError());
        } else {
            listApplications.setVisibility(View.VISIBLE);
            txtAppsError.setVisibility(View.GONE);
            List<AppVersions> appsVersions = result.getValue();
            deviceAppsAdapter.clear();
            for (AppVersions app : appsVersions) {
                if (app.app.ui && app.installed())
                    deviceAppsAdapter.add(app);
            }
        }
    }

    private void onAppsLoadedError(String error) {
        listApplications.setVisibility(View.GONE);
        txtAppsError.setVisibility(View.VISIBLE);
        txtAppsError.setText(error);
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
        } else if (id == R.id.action_get_access) {
            getAccess();
        } else if (id == R.id.action_manage_apps) {
            Intent intent = new Intent(this, DeviceAppStoreActivity.class);
            intent.putExtra(SyncloudApplication.DOMAIN, domain);
            startActivityForResult(intent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    private void openApp(String appId) {
        if (appRegistry.containsKey(appId)) {
            Intent intent = new Intent(this, appRegistry.get(appId));
            intent.putExtra(SyncloudApplication.DOMAIN, domain);
            startActivity(intent);
        }
    }

    public void deactivate() {
        new ProgressAsyncTask<Void, String>() {}
                .setTitle("Dropping device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public AsyncResult<String> run(Void... args) {
                        String email = preferences.getEmail();
                        String password = preferences.getPassword();
                        redirectService.dropDevice(email, password, domain.userDomain());
                        return AsyncResult.value("Success");
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<String>() {
                    @Override
                    public void run(String result) {
                        finish();
                    }
                })
                .execute();
    }

    private void onGetAccess(Credentials result) {
        Key key = new Key(domain.device().id().mac_address, result.key());
        KeysStorage keysStorage = this.application.keysStorage();
        keysStorage.upsert(key);
    }

    public void getAccess() {
        new ProgressAsyncTask<Void, Credentials>() {}
                .setTitle("Getting access")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, Credentials>() {
                    @Override
                    public AsyncResult<Credentials> run(Void... args) {
                        Endpoint endpoint = new Endpoint(domain.device().localEndpoint().host(), SshRunner.SSH_SERVER_PORT);
                        ConnectionPoint localConnectionPoint = new ConnectionPoint(endpoint, getStandardCredentials());
                        Optional<Credentials> credentialsResult = server.get_access(simple(localConnectionPoint));
                        return AsyncResult.value(credentialsResult.get());
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<Credentials>() {
                    @Override
                    public void run(Credentials result) {
                        onGetAccess(result);
                    }
                })
                .execute();
    }

    public void shareDevice() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, domain.userDomain());
        String body = "";
        body += "Host: " + domain.userDomain() + "\n";
        body += "KEY:\n\n" + domain.device().credentials().key() + "\n";
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }
}
