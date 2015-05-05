package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.android.ui.dialog.ErrorDialog;
import org.syncloud.common.SyncloudResultException;
import org.syncloud.platform.server.Server;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.ConnectionPoint;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.Endpoint;
import org.syncloud.platform.ssh.model.Identification;
import org.syncloud.platform.ssh.model.Key;
import org.syncloud.common.ParameterMessages;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.syncloud.platform.ssh.SimpleConnectionPointProvider.simple;
import static org.syncloud.platform.ssh.model.Credentials.getStandardCredentials;


public class DeviceActivateActivity extends Activity {

    private static Logger logger = Logger.getLogger(DeviceActivateActivity.class);

    private Preferences preferences;

    private CommunicationDialog progress;
    private TextView txtDeviceTitle;
    private TextView txtMacAddress;
    private TextView txtStatusValue;

    private EditText editUserDomain;
    private TextView txtMainDomain;

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

        txtMainDomain = (TextView) findViewById(R.id.txt_main_domain);
        editUserDomain = (EditText) findViewById(R.id.edit_user_domain);

        progress = new CommunicationDialog(this);

        this.application = (SyncloudApplication) getApplication();

        Endpoint endpoint = (Endpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
        identification = (Identification) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ID);

        connectionPoint = simple(new ConnectionPoint(endpoint, getStandardCredentials()));

        preferences = application.getPreferences();

        ssh = new SshRunner();
        server = new Server(ssh);

        txtDeviceTitle.setText(this.identification.title);
        txtMacAddress.setText(this.identification.mac_address);

        layoutMacAddress.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);

        txtStatusValue.setText("checking...");

        txtMainDomain.setText("."+preferences.getDomain());

        status();
    }

    private EditText getControl(String parameter) {
        if (parameter.equals("user_domain"))
            return editUserDomain;
        return null;
    }

    private boolean validate() {
        editUserDomain.setError(null);

        final String domain = editUserDomain.getText().toString().toLowerCase();

        if (domain == null || domain.isEmpty()) {
            editUserDomain.setError("Enter domain");
            editUserDomain.requestFocus();
            return false;
        }

        return true;
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
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public String run(Void... args) {
                        return server.userDomain(connectionPoint);
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<String>() {
                    @Override
                    public void run(AsyncResult<String> result) {
                        if (!result.hasValue()) {
                            txtStatusValue.setText("Unable to get user domain");
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
        if (!validate()) return;

        final String email = preferences.getEmail();
        final String pass = preferences.getPassword();
        final String domain = editUserDomain.getText().toString().toLowerCase();

        new ProgressAsyncTask<Void, String>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public String run(Void... args) {
                        doActivate(email, pass, domain);
                        return "placeholder";
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<String>() {
                    @Override
                    public void run(AsyncResult<String> result) {
                        onActivate(result);
                    }
                })
                .execute();
    }

    private void onActivate(AsyncResult<String> result) {
        if (result.hasValue()) {
            finish();
        } else {
            if (result.getException() instanceof SyncloudResultException) {
                SyncloudResultException apiError = (SyncloudResultException)result.getException();
                List<ParameterMessages> messages = apiError.result.parameters_messages;
                if (messages != null && messages.size() > 0) {
                    for (ParameterMessages pm: messages) {
                        EditText control = getControl(pm.parameter);
                        if (control != null) {
                            String message = join(pm.messages, '\n');
                            control.setError(message);
                            control.requestFocus();
                        }
                    }

                    return;
                }
            }

            new ErrorDialog(this, "Unable to activate").show();
        }
    }

    private void doActivate(final String email, final String pass, final String domain) {
        logger.info("activate " + domain);

        Credentials credentials = server.activate(
                connectionPoint,
                preferences.getVersion(),
                preferences.getDomain(),
                preferences.getApiUrl(),
                email,
                pass,
                domain);

        Key key = new Key(identification.mac_address, credentials.key());
        KeysStorage keysStorage = this.application.keysStorage();
        keysStorage.upsert(key);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }

}
