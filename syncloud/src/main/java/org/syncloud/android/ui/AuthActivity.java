package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.redirect.IUserService;
import org.syncloud.redirect.UserResult;

public class AuthActivity extends ActionBarActivity {

    private Preferences preferences;

    private ProgressBar progressBar;
    private LinearLayout signInOrOut;

    private IUserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        SyncloudApplication application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        userService = application.userServiceCached();

        progressBar = (ProgressBar) findViewById(R.id.progress_check_user);
        signInOrOut = (LinearLayout) findViewById(R.id.sign_in_or_up);

        TextView learnMoreText = (TextView) findViewById(R.id.auth_learn_more);
        learnMoreText.setMovementMethod(LinkMovementMethod.getInstance());

        if (preferences.hasCredentials()) {
            new CheckCredentialsTask(preferences).execute();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auth, menu);
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

    public void signIn(View view) {
        Intent credentialsIntent = new Intent(this, AuthCredentialsActivity.class);
        credentialsIntent.putExtra(AuthCredentialsActivity.PARAM_PURPOSE, AuthCredentialsActivity.PURPOSE_SIGN_IN);
        startActivityForResult(credentialsIntent, 1);
    }

    public void signUp(View view) {
        Intent credentialsIntent = new Intent(this, AuthCredentialsActivity.class);
        credentialsIntent.putExtra(AuthCredentialsActivity.PARAM_PURPOSE, AuthCredentialsActivity.PURPOSE_REGISTER);
        startActivityForResult(credentialsIntent, 1);
    }

    public class CheckCredentialsTask extends AsyncTask<Void, Void, UserResult> {
        private Preferences preferences;

        public CheckCredentialsTask(Preferences preferences) {
            this.preferences = preferences;
        }

        @Override
        protected void onPreExecute() {
            signInOrOut.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected UserResult doInBackground(Void... voids) {
            String email = preferences.getEmail();
            String password = preferences.getPassword();
            UserResult result = userService.getUser(email, password);
            return result;
        }

        @Override
        protected void onPostExecute(UserResult result) {
            progressBar.setVisibility(View.INVISIBLE);
            if (result.hasError()) {
                signInOrOut.setVisibility(View.VISIBLE);

                Intent intent = new Intent(AuthActivity.this, AuthCredentialsActivity.class);
                intent.putExtra(AuthCredentialsActivity.PARAM_PURPOSE, AuthCredentialsActivity.PURPOSE_SIGN_IN);
                intent.putExtra(AuthCredentialsActivity.PARAM_CHECK_EXISTING, true);
                startActivityForResult(intent, 1);
            } else {
                Intent intent = new Intent(AuthActivity.this, DevicesSavedActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

}
