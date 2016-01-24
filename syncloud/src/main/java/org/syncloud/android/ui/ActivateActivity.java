package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.Progress;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.core.common.ParameterMessages;
import org.syncloud.android.core.common.SyncloudResultException;
import org.syncloud.android.core.platform.Internal;
import org.syncloud.android.core.platform.model.Endpoint;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.ErrorDialog;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

public class ActivateActivity extends Activity {
    private static Logger logger = Logger.getLogger(ActivateActivity.class);

    private Preferences preferences;
    private Endpoint endpoint;

    private ProgressBar viewProgress;
    private EditText editDomain;
    private EditText editLogin;
    private EditText editPassword;

    private Progress progress = new ProgressImpl();

    private Internal deviceInternal = new Internal();

    private SyncloudApplication application;

    class ProgressImpl implements Progress {
        @Override
        public void start() {
            viewProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void stop() {
            viewProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        public void error(String message) {

        }

        @Override
        public void title(String title) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        editDomain = (EditText) findViewById(R.id.edit_domain);
        editLogin = (EditText) findViewById(R.id.edit_login);
        editPassword = (EditText) findViewById(R.id.edit_password);
        viewProgress = (ProgressBar) findViewById(R.id.progress);

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();

        endpoint = (Endpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
    }

    private EditText getControl(String parameter) {
        if (parameter.equals("user_domain"))
            return editDomain;
        return null;
    }

    private boolean validate() {
        editDomain.setError(null);

        final String domain = editDomain.getText().toString().toLowerCase();
        final String login = editLogin.getText().toString();
        final String password = editPassword.getText().toString();

        if (domain == null || domain.isEmpty()) {
            editDomain.setError("Enter domain");
            editDomain.requestFocus();
            return false;
        }

        if (login == null || login.isEmpty()) {
            editLogin.setError("Enter device login");
            editLogin.requestFocus();
            return false;
        }

        if (password == null || password.isEmpty()) {
            editPassword.setError("Enter device password");
            editPassword.requestFocus();
            return false;
        }

        return true;
    }

    public void activate(View view) {
        if (!validate()) return;

        final String domain = editDomain.getText().toString().toLowerCase();
        final String login = editLogin.getText().toString();
        final String password = editPassword.getText().toString();

        new ProgressAsyncTask<Void, String>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public String run(Void... args) {
                        doActivate(domain, login, password);
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

    private void doActivate(String domain, String login, String password) {
        logger.info("activate " + domain);

        deviceInternal.activate(
            endpoint.host(),
            application.getPreferences().getApiUrl(),
            application.getPreferences().getDomain(),
            preferences.getEmail(),
            preferences.getPassword(),
            domain,
            login,
            password
        );
    }

    private void onActivate(AsyncResult<String> result) {
        if (result.hasValue()) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            boolean errorShown= false;
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
                            errorShown = true;
                        }
                    }
                }

            }
            if (errorShown)
                return;

            new ErrorDialog(this, "Unable to activate").show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        }
        return super.onOptionsItemSelected(item);
    }
}
