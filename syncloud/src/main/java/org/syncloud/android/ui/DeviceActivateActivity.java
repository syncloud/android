package org.syncloud.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Commands;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.remote.RemoteAccessManager;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.IdentifiedEndpoint;

import java.util.List;

import static org.syncloud.common.model.Result.error;
import static org.syncloud.common.model.Result.value;
import static org.syncloud.ssh.model.Credentials.getStandardCredentials;


public class DeviceActivateActivity extends Activity {
    private IdentifiedEndpoint endpoint;
    private Preferences preferences;
    private Device device;

    private CommunicationDialog progress;
    private TextView url;
    private LinearLayout domainSettings;
    private Button deactivateButton;
    private EditText userDomainText;
    private Sam sam;
    private InsiderManager insider;
    private RemoteAccessManager accessManager;

    private String domainName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);

        SyncloudApplication application = (SyncloudApplication) getApplication();

        endpoint = (IdentifiedEndpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        device = new Device(null, null, endpoint.endpoint(), getStandardCredentials());

        preferences = application.getPreferences();

        progress = new CommunicationDialog(this);
        Ssh ssh = application.createSsh();
        sam = new Sam(ssh);
        insider = new InsiderManager(ssh);
        accessManager = new RemoteAccessManager(insider, ssh);
        url = (TextView) findViewById(R.id.device_url);
        deactivateButton = (Button) findViewById(R.id.name_deactivate);
        domainSettings = (LinearLayout) findViewById(R.id.domain_settings);

        userDomainText = (EditText) findViewById(R.id.user_domain);

        url.setText("[user]." + preferences.getDomain());

        status();
    }

    private void showDomainError(String message) {
        userDomainText.setError(message);
        userDomainText.requestFocus();
    }

    private String getDomainName() {
        String domainFromEdit = userDomainText.getText().toString();
        if (!domainFromEdit.isEmpty())
            return domainFromEdit;
        else
            return domainName;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.dns, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void status() {
        new ProgressAsyncTask<Void, String>()
                .setTitle("Checking status")
                .setProgress(progress)
                .showError(false)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public Result<String> run(Void... args) {
                        return insider.userDomain(device);
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<String>() {
                    @Override
                    public void run(Result<String> result) {
                        if (result.hasError()) {
                            domainSettings.setVisibility(View.VISIBLE);
                            deactivateButton.setVisibility(View.GONE);
                        } else {
                            domainSettings.setVisibility(View.GONE);
                            deactivateButton.setVisibility(View.VISIBLE);
                            domainName = result.getValue();
                            url.setText(domainName+"."+preferences.getDomain());

                        }
                    }
                })
                .execute();
    }

    public void deactivate(View view) {
        new ProgressAsyncTask<Void, String>()
                .setTitle("Deactivating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public Result<String> run(Void... args) {
                        return insider.dropDomain(device);
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<String>() {
                    @Override
                    public void run(String result) {
                        status();
                    }
                })
                .execute();
    }

    public void activate(View view) {
        final String email = preferences.getEmail();
        final String pass = preferences.getPassword();
        final String domain = getDomainName();

        if (domain == null || domain.isEmpty()) {
            showDomainError("Enter domain");
            return;
        }

        new ProgressAsyncTask<Void, Void>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, Void>() {
                    @Override
                    public Result<Void> run(Void... args) {
                        return doActivate(email, pass, domain);
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<Void>() {
                    @Override
                    public void run(Void result) {
                        status();
                    }
                })
                .execute();
    }

    private Result<Void> doActivate(String email, String pass, String domain) {
        Result<List<AppVersions>> updateResult = sam.update(device);
        if (updateResult.hasError())
            return error(updateResult.getError());

        Result<String> upgradeAllResult = sam.run(device, Commands.upgrade_all);
        if (upgradeAllResult.hasError())
            return error(upgradeAllResult.getError());

        Result<String> redirectResult = insider.setRedirectInfo(device, preferences.getDomain(), preferences.getApiUrl());
        if (redirectResult.hasError())
            return error(redirectResult.getError());

        Result<String> acquireDomainResult = insider.acquireDomain(device, email, pass, domain);
        if (acquireDomainResult.hasError())
            return error(acquireDomainResult.getError());

        Result<Device> remoteAccessResult = accessManager.enable(device, preferences.getDomain());
        if (remoteAccessResult.hasError())
            return error(remoteAccessResult.getError());

        Db db = ((SyncloudApplication) getApplication()).getDb();
        db.insert(remoteAccessResult.getValue());

        return value(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

}
