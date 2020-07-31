package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
import org.syncloud.android.core.redirect.IUserService;
import org.syncloud.android.core.redirect.model.User;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;

import static org.apache.commons.lang3.StringUtils.join;

public class AuthCredentialsActivity extends AppCompatActivity {

    private static Logger logger = Logger.getLogger(AuthCredentialsActivity.class);

    public static final String PARAM_PURPOSE = "paramPurpose";
    public static final String PARAM_CHECK_EXISTING = "paramCheckExisting";

    public static final String PURPOSE_SIGN_IN = "purposeSignIn";
    public static final String PURPOSE_REGISTER = "purposeRegister";


    private Preferences preferences;
    private IUserService userService;

    private LinearLayout emailLoginFormView;
    private EditText emailView;
    private EditText passwordView;
    private Button signInButton;
    private CircleProgressBar progressBar;


    private String purpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_auth_credentials);

        SyncloudApplication application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        userService = application.userServiceCached();

        emailLoginFormView = (LinearLayout) findViewById(R.id.email_login_form);

        emailView = (EditText) findViewById(R.id.email);

        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        progressBar = (CircleProgressBar) findViewById(R.id.progress);
        progressBar.setColorSchemeResources(R.color.logo_blue, R.color.logo_green);

        Intent intent = getIntent();
        purpose = intent.getStringExtra(PARAM_PURPOSE);

        if (purpose.equals(PURPOSE_SIGN_IN)) {
            setTitle(R.string.action_sign_in);
            signInButton.setText(R.string.action_sign_in);
        }
        if (purpose.equals(PURPOSE_REGISTER)) {
            setTitle(R.string.action_sign_up);
            signInButton.setText(R.string.action_sign_up);
        }

        if (preferences.hasCredentials()) {
            String email = preferences.getRedirectEmail();
            String password = preferences.getRedirectPassword();

            emailView.setText(email);
            passwordView.setText(password);

            boolean checkExisting = intent.getBooleanExtra(PARAM_CHECK_EXISTING, false);
            if (checkExisting) {
                new AlertDialog.Builder(AuthCredentialsActivity.this)
                        .setTitle(getString(R.string.check_credentials))
                        .setMessage(getString(R.string.sign_in_failed))
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    private Progress progress = new ProgressImpl();

    public class ProgressImpl extends Progress.Empty {
        @Override
        public void start() {
            showProgress(true);
        }

        @Override
        public void stop() {
            showProgress(false);
        }
    }

    private void setLayoutEnabled(LinearLayout layout, boolean enabled) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            view.setEnabled(enabled);
        }
    }

    public void showProgress(final boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        setLayoutEnabled(emailLoginFormView, !show);
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

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean validate() {
        emailView.setError(null);
        passwordView.setError(null);

        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        boolean hasErrors = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            hasErrors = true;
        } else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            hasErrors = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            hasErrors = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            hasErrors = true;
        }

        if (hasErrors) {
            focusView.requestFocus();
            return false;
        }

        return true;
    }

    private void attemptLogin() {
        if (!validate()) return;

        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        final boolean register = purpose.equals(PURPOSE_REGISTER);

        new ProgressAsyncTask<Void, User>()
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, User>() {
                    @Override
                    public User run(Void... args) { return doUserTask(register, email, password); }
                })
                .onCompleted(new ProgressAsyncTask.Completed<User>() {
                    @Override
                    public void run(AsyncResult<User> result) {
                        onUserTaskCompleted(result);
                    }
                })
                .execute();
    }

    private User doUserTask(boolean register, String email, String password) {
        User user;
        if (register) {
            user = userService.createUser(email, password);
        } else {
            user = userService.getUser(email, password);
        }
        return user;
    }

    private void onUserTaskCompleted(AsyncResult<User> result) {
        if (result.hasValue()) {
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            preferences.setCredentials(email, password);
            finishSuccess();
        } else {
            showError(result.getException());
        }
    }

    private void showErrorDialog(String message) {
        boolean register = purpose.equals(PURPOSE_REGISTER);
        String errorMessage;
        if (register)
            errorMessage = "Unable to register new user";
        else
            errorMessage = "Unable to login";

        new AlertDialog.Builder(AuthCredentialsActivity.this)
                .setTitle("Failed")
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private EditText getControl(String parameter) {
        if (parameter.equals("email"))
            return emailView;
        if (parameter.equals("password"))
            return passwordView;
        return null;
    }

    private void showError(Throwable error) {
        if (error instanceof SyncloudResultException) {
            SyncloudResultException apiError = (SyncloudResultException)error;
            if (apiError.result.parameters_messages != null) {
                for (ParameterMessages pm: apiError.result.parameters_messages) {
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
        logger.error("auth error", error);
        showErrorDialog(error.getMessage());
    }

    private void finishSuccess() {
        Intent intent = new Intent(this, DevicesSavedActivity.class);
        startActivity(intent);
        setResult(Activity.RESULT_OK, new Intent(AuthCredentialsActivity.this, AuthActivity.class));
        finish();
    }

}



