package org.syncloud.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
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

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.common.model.Result;
import org.syncloud.redirect.UserService;

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
            authTask = new UserTask(preferences, purpose.equals(PURPOSE_REGISTER), email, password);
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

    public class UserTask extends AsyncTask<Void, Void, String> {

        private final Preferences preferences;
        boolean register = false;
        private final String email;
        private final String password;


        UserTask(Preferences preferences, boolean register, String email, String password) {
            this.preferences = preferences;
            this.register = register;
            this.email = email;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... params) {

            if (register) {
                Result<String> user = UserService.createUser(email, password, preferences.getApiUrl());
                if (user.hasError())
                    return user.getError();
            } else {
                Result<Boolean> user = UserService.getUser(email, password, preferences.getApiUrl());
                if (user.hasError())
                    return getString(R.string.sign_in_failed);
            }

            preferences.setCredentials(email, password);

            return null;
        }

        @Override
        protected void onPostExecute(final String error) {
            authTask = null;
            showProgress(false);

            if (error == null) {
                Intent intent = new Intent(AuthCredentialsActivity.this, DevicesSavedActivity.class);
                startActivity(intent);
                setResult(Activity.RESULT_OK, new Intent(AuthCredentialsActivity.this, AuthActivity.class));
                finish();
            } else {
                new AlertDialog.Builder(AuthCredentialsActivity.this)
                        .setTitle("Failed")
                        .setMessage(error)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
//                passwordView.setError(getString(R.string.error_incorrect_password));
//                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }
}



