package org.syncloud.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.remote.RemoteAccessManager;
import org.syncloud.apps.sam.AppVersions;
import org.syncloud.apps.sam.Commands;
import org.syncloud.apps.sam.Sam;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.IdentifiedEndpoint;

import java.util.List;

import static org.syncloud.common.model.Result.VOID;
import static org.syncloud.common.model.Result.error;
import static org.syncloud.ssh.model.Credentials.getStandardCredentials;


public class DeviceActivateActivity extends Activity {

    private static Logger logger = Logger.getLogger(DeviceActivateActivity.class);

    private IdentifiedEndpoint endpoint;
    private Preferences preferences;
    private Device device;

    private CommunicationDialog progress;
    private TextView txtDeviceTitle;
    private TextView txtMacAddress;
    private TextView txtStatusValue;

    private Button btnActivate;
    private EditText editUserDomain;
    private TextView txtMainDomain;

    private Sam sam;
    private InsiderManager insider;
    private RemoteAccessManager accessManager;
    private LinearLayout layoutMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);

        txtDeviceTitle = (TextView) findViewById(R.id.txt_device_title);
        txtStatusValue = (TextView) findViewById(R.id.txt_status_value);
        txtMacAddress = (TextView) findViewById(R.id.txt_mac_address);
        layoutMacAddress = (LinearLayout) findViewById(R.id.layout_activate_mac_address);

        btnActivate = (Button) findViewById(R.id.btn_activate);

        txtMainDomain = (TextView) findViewById(R.id.txt_main_domain);
        editUserDomain = (EditText) findViewById(R.id.edit_user_domain);

        progress = new CommunicationDialog(this);

        SyncloudApplication application = (SyncloudApplication) getApplication();

        endpoint = (IdentifiedEndpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        device = new Device(
                endpoint.id().mac_address,
                endpoint.id(),
                null,
                endpoint.endpoint(),
                getStandardCredentials());

        preferences = application.getPreferences();

        Ssh ssh = application.createSsh();
        sam = new Sam(ssh, preferences);
        insider = new InsiderManager(ssh);
        accessManager = new RemoteAccessManager(insider, ssh);

        txtDeviceTitle.setText(device.id().title);
        txtMacAddress.setText(device.id().mac_address);

        layoutMacAddress.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);

        txtStatusValue.setText("checking...");

        txtMainDomain.setText("."+preferences.getDomain());

        status();
    }

    private void showDomainError(String message) {
        editUserDomain.setError(message);
        editUserDomain.requestFocus();
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
        logger.info("status");
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
                            txtStatusValue.setText("not yet");
                        } else {
                            String domainName = result.getValue();
                            String fullDomainName = domainName+"."+preferences.getDomain();
                            txtStatusValue.setText(fullDomainName);
                            editUserDomain.setText(domainName);
                        }
                    }
                })
                .execute();
    }

    public void activate(View view) {
        final String email = preferences.getEmail();
        final String pass = preferences.getPassword();
        final String domain = editUserDomain.getText().toString();

        if (domain == null || domain.isEmpty()) {
            showDomainError("Enter domain");
            return;
        }

        new ProgressAsyncTask<Void, Result.Void>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, Result.Void>() {
                    @Override
                    public Result<Result.Void> run(Void... args) {
                        return doActivate(email, pass, domain);
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

    private Result<Result.Void> doActivate(final String email, final String pass, final String domain) {
        logger.info("activate");

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

        return VOID;
//        return sam.update(device).flatMap(new Result.Function<List<AppVersions>, Result<String>>() {
//            @Override
//            public Result<String> apply(List<AppVersions> input) throws Exception {
//                return sam.run(device, Commands.upgrade_all);
//            }
//        }).flatMap(new Result.Function<String, Result<String>>() {
//            @Override
//            public Result<String> apply(String input) throws Exception {
//                return insider.setRedirectInfo(device, preferences.getDomain(), preferences.getApiUrl());
//            }
//        }).flatMap(new Result.Function<String, Result<String>>() {
//            @Override
//            public Result<String> apply(String input) throws Exception {
//                return insider.acquireDomain(device, email, pass, domain);
//            }
//        }).flatMap(new Result.Function<String, Result<Device>>() {
//            @Override
//            public Result<Device> apply(String input) throws Exception {
//                return accessManager.enable(device, preferences.getDomain());
//            }
//        }).flatMap(new Result.Function<Device, Result<Result.Void>>() {
//            @Override
//            public Result<Result.Void> apply(Device input) throws Exception {
//                Db db = ((SyncloudApplication) getApplication()).getDb();
//                db.upsert(input);
//                return VOID;
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

}
