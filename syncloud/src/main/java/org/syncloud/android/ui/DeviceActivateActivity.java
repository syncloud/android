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

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.insider.InsiderManager;
import org.syncloud.apps.sam.Sam;
import org.syncloud.apps.server.Server;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.ConnectionPoint;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;
import org.syncloud.ssh.model.Key;

import static org.syncloud.android.tasks.AsyncResult.value;
import static org.syncloud.ssh.SimpleConnectionPointProvider.simple;
import static org.syncloud.ssh.model.Credentials.getStandardCredentials;


public class DeviceActivateActivity extends Activity {

    private static Logger logger = Logger.getLogger(DeviceActivateActivity.class);

    private Preferences preferences;

    private CommunicationDialog progress;
    private TextView txtDeviceTitle;
    private TextView txtMacAddress;
    private TextView txtStatusValue;

    private Button btnActivate;
    private EditText editUserDomain;
    private TextView txtMainDomain;

    private Sam sam;
    private InsiderManager insider;
    private LinearLayout layoutMacAddress;

    private SyncloudApplication application;

    private Identification identification;
    private ConnectionPointProvider connectionPoint;
    private SshRunner ssh;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_activate);

        txtDeviceTitle = (TextView) findViewById(R.id.txt_bold_title);
        txtStatusValue = (TextView) findViewById(R.id.txt_status_value);
        txtMacAddress = (TextView) findViewById(R.id.txt_second_line);
        layoutMacAddress = (LinearLayout) findViewById(R.id.layout_activate_mac_address);

        btnActivate = (Button) findViewById(R.id.btn_activate);

        txtMainDomain = (TextView) findViewById(R.id.txt_main_domain);
        editUserDomain = (EditText) findViewById(R.id.edit_user_domain);

        progress = new CommunicationDialog(this);

        this.application = (SyncloudApplication) getApplication();

        Endpoint endpoint = (Endpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        identification = (Identification) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ID);

        connectionPoint = simple(new ConnectionPoint(endpoint, getStandardCredentials()));

        preferences = application.getPreferences();

        ssh = new SshRunner();
        sam = new Sam(ssh, preferences);
        server = new Server(ssh);
        insider = new InsiderManager();

        txtDeviceTitle.setText(this.identification.title);
        txtMacAddress.setText(this.identification.mac_address);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                    public AsyncResult<String> run(Void... args) {
                        return new AsyncResult<String>(
                                insider.userDomain(connectionPoint),
                                "unable to get user domain");
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<String>() {
                    @Override
                    public void run(AsyncResult<String> result) {
                        if (result.hasError()) {
                            txtStatusValue.setText("not yet");
                        } else {
                            String domainName = result.getValue();
                            String fullDomainName = domainName + "." + preferences.getDomain();
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

        new ProgressAsyncTask<Void, String>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public AsyncResult<String> run(Void... args) {
                        return doActivate(email, pass, domain) ?
                                value("Activated") :
                                AsyncResult.<String>error("Unable to activate");
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

    private boolean doActivate(final String email, final String pass, final String domain) {
        logger.info("activate " + domain);

        Optional<Credentials> credentialsResult = server.activate(
                connectionPoint,
                preferences.getVersion(),
                preferences.getDomain(),
                preferences.getApiUrl(),
                email,
                pass,
                domain);

        if (!credentialsResult.isPresent()) {
            logger.error("unable to enable remote access");
            return false;
        }

        Key key = new Key(identification.mac_address, credentialsResult.get().key());
        KeysStorage keysStorage = this.application.keysStorage();
        keysStorage.upsert(key);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

}
