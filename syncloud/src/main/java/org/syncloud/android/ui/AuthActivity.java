package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.syncloud.redirect.UserService;
import org.syncloud.redirect.model.RestResult;

public class AuthActivity extends Activity {

    private Preferences preferences;

    private ProgressBar progressBar;
    private LinearLayout signInOrOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        preferences = ((SyncloudApplication) getApplication()).getPreferences();

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.auth, menu);
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

    public class CheckCredentialsTask extends AsyncTask<Void, Void, RestResult<String>> {
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
        protected RestResult<String> doInBackground(Void... voids) {
            String email = preferences.getEmail();
            String password = preferences.getPassword();
            RestResult<String> result = UserService.getUser(email, password, preferences.getApiUrl());
            return result;
        }

        @Override
        protected void onPostExecute(RestResult<String> result) {
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
