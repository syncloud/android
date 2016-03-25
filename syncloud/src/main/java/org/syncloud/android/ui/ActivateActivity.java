package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

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

public class ActivateActivity extends ActionBarActivity {
    private static Logger logger = Logger.getLogger(ActivateActivity.class);

    private Preferences preferences;
    private Endpoint endpoint;

    private CircleProgressBar progressBar;
    private EditText editDomain;
    private EditText editLogin;
    private EditText editPassword;
    private TextView labelDomain;

    private LinearLayout viewActivateForm;

    private Progress progress = new ProgressImpl();

    private Internal deviceInternal = new Internal();

    private SyncloudApplication application;

    class ProgressImpl implements Progress {
        @Override
        public void start() {
            setLayoutEnabled(viewActivateForm, false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void stop() {
            setLayoutEnabled(viewActivateForm, true);
            progressBar.setVisibility(View.INVISIBLE);
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
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_activate);

        editDomain = (EditText) findViewById(R.id.edit_domain);
        editLogin = (EditText) findViewById(R.id.edit_login);
        editPassword = (EditText) findViewById(R.id.edit_password);
        progressBar = (CircleProgressBar) findViewById(R.id.progress);
        labelDomain = (TextView) findViewById(R.id.label_domain);
        viewActivateForm = (LinearLayout) findViewById(R.id.activity_form);

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();

        progressBar.setColorSchemeResources(R.color.logo_blue, R.color.logo_green);

        labelDomain.setText("."+preferences.getMainDomain());

        endpoint = (Endpoint) getIntent().getSerializableExtra(SyncloudApplication.DEVICE_ENDPOINT);
    }

    private void setLayoutEnabled(LinearLayout layout, boolean enabled) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            if (view instanceof LinearLayout)
                setLayoutEnabled((LinearLayout)view, enabled);
            view.setEnabled(enabled);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        labelDomain.setText("."+preferences.getMainDomain());
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

        final String userDomain = editDomain.getText().toString().toLowerCase();
        final String deviceUsername = editLogin.getText().toString();
        final String devicePassword = editPassword.getText().toString();

        new ProgressAsyncTask<Void, String>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public String run(Void... args) {
                        doActivate(userDomain, deviceUsername, devicePassword);
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

    private void doActivate(String userDomain, String deviceUsername, String devicePassword) {
        logger.info("activate " + userDomain);

        deviceInternal.activate(
            endpoint.host(),
            application.getPreferences().getMainDomain(),
            preferences.getRedirectEmail(),
            preferences.getRedirectPassword(),
            userDomain,
            deviceUsername,
            devicePassword
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
