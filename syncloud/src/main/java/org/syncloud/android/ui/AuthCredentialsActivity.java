package org.syncloud.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.redirect.UserService;
import org.syncloud.redirect.model.ParameterMessages;
import org.syncloud.redirect.model.RestError;
import org.syncloud.redirect.model.RestResult;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.join;

public class AuthCredentialsActivity extends Activity {

    public static final String PARAM_PURPOSE = "paramPurpose";
    public static final String PARAM_CHECK_EXISTING = "paramCheckExisting";

    public static final String PURPOSE_SIGN_IN = "purposeSignIn";
    public static final String PURPOSE_REGISTER = "purposeRegister";


    private UserTask authTask = null;
    private Preferences preferences;

    private LinearLayout emailLoginFormView;
    private EditText emailView;
    private EditText passwordView;
    private Button signInButton;
    private View progressView;
    private String purpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_credentials);

        preferences = ((SyncloudApplication) getApplication()).getPreferences();

        emailLoginFormView = (LinearLayout) findViewById(R.id.email_login_form);

        emailView = (EditText) findViewById(R.id.email);
        emailView.setText(suggestEMail());

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

        progressView = findViewById(R.id.login_progress);

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
            String email = preferences.getEmail();
            String password = preferences.getPassword();

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

    private String suggestEMail() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return  account.name;
            }
        }
        return "";
    }

    private EditText getControl(String parameter) {
        if (parameter.equals("email"))
            return emailView;
        if (parameter.equals("password"))
            return passwordView;
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        }
        return super.onOptionsItemSelected(item);
    }

    public void attemptLogin() {
        if (authTask != null) {
            return;
        }

        emailView.setError(null);
        passwordView.setError(null);

        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            boolean register = purpose.equals(PURPOSE_REGISTER);
            authTask = new UserTask(register, preferences, email, password);
            authTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void showProgress(final boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        setLayoutEnabled(emailLoginFormView, !show);
    }

    private void setLayoutEnabled(LinearLayout layout, boolean enabled) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            view.setEnabled(enabled);
        }
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(AuthCredentialsActivity.this)
                .setTitle("Failed")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showError(RestError error) {
        if (error.parameters_messages == null) {
            showErrorDialog(error.message);
        } else {
            for (ParameterMessages pm: error.parameters_messages) {
                EditText control = getControl(pm.parameter);
                if (control != null) {
                    String message = join(pm.messages, '\n');
                    control.setError(message);
                    control.requestFocus();
                }
            }
        }
    }

    private void finishSuccess() {
        Intent intent = new Intent(this, DevicesSavedActivity.class);
        startActivity(intent);
        setResult(Activity.RESULT_OK, new Intent(AuthCredentialsActivity.this, AuthActivity.class));
        finish();
    }

    public class UserTask extends AsyncTask<Void, Void, RestResult<String>> {

        private final boolean register;
        private final Preferences preferences;
        private final String email;
        private final String password;


        UserTask(boolean register, Preferences preferences, String email, String password) {
            this.register = register;
            this.preferences = preferences;
            this.email = email;
            this.password = password;
        }

        @Override
        protected RestResult<String> doInBackground(Void... params) {
            RestResult<String> result;
            if (register) {
                result = UserService.createUser(email, password, preferences.getApiUrl());
            } else {
                result = UserService.getUser(email, password, preferences.getApiUrl());
            }
            if (!result.hasError())
                preferences.setCredentials(email, password);
            return result;
        }

        @Override
        protected void onPostExecute(final RestResult<String> result) {
            authTask = null;
            showProgress(false);

            if (result.hasError())
                showError(result.getError());
            else
                finishSuccess();
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }

}



