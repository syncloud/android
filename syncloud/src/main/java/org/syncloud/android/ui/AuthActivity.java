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
import org.syncloud.common.model.Result;
import org.syncloud.redirect.UserService;

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
            signInOrOut.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    String email = preferences.getEmail();
                    String password = preferences.getPassword();
                    final Result<Boolean> user = UserService.getUser(email, password, preferences.getApiUrl());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (user.hasError()) {
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
                    });


                }
            });
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

}
