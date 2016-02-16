package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.syncloud.android.Preferences;
import org.syncloud.android.Progress;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.core.redirect.IUserService;
import org.syncloud.android.core.redirect.model.User;

public class AuthActivity extends Activity {

    private Preferences preferences;

    private CircleProgressBar progressBar;

    private LinearLayout signInOrOut;

    private IUserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        SyncloudApplication application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        userService = application.userServiceCached();

        progressBar = (CircleProgressBar) findViewById(R.id.progress);
        progressBar.setColorSchemeResources(R.color.logo_blue, R.color.logo_green);

        signInOrOut = (LinearLayout) findViewById(R.id.sign_in_or_up);

        TextView learnMoreText = (TextView) findViewById(R.id.auth_learn_more);
        learnMoreText.setMovementMethod(LinkMovementMethod.getInstance());

        proceedWithLogin();
    }

    private Progress progress = new ProgressImpl();

    public class ProgressImpl extends Progress.Empty {
        @Override
        public void start() {
            signInOrOut.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void stop() {
            signInOrOut.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void proceedWithLogin() {
        if (preferences.hasCredentials()) {
            login();
        }

    }

    private void login() {
        final String email = preferences.getRedirectEmail();
        final String password = preferences.getRedirectPassword();

        new ProgressAsyncTask<Void, User>()
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, User>() {
                    @Override
                    public User run(Void... args) { return userService.getUser(email, password); }
                })
                .onCompleted(new ProgressAsyncTask.Completed<User>() {
                    @Override
                    public void run(AsyncResult<User> result) {
                        onLoginCompleted(result);
                    }
                })
                .execute();
    }

    private void onLoginCompleted(AsyncResult<User> result) {
        if (result.hasValue()) {
            Intent intent = new Intent(AuthActivity.this, DevicesSavedActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(AuthActivity.this, AuthCredentialsActivity.class);
            intent.putExtra(AuthCredentialsActivity.PARAM_PURPOSE, AuthCredentialsActivity.PURPOSE_SIGN_IN);
            intent.putExtra(AuthCredentialsActivity.PARAM_CHECK_EXISTING, true);
            startActivityForResult(intent, REQUEST_AUTHENTICATE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static int REQUEST_AUTHENTICATE = 1;
    public static int REQUEST_CHECK = 2;


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
        startActivityForResult(credentialsIntent, REQUEST_AUTHENTICATE);
    }

    public void signUp(View view) {
        Intent credentialsIntent = new Intent(this, AuthCredentialsActivity.class);
        credentialsIntent.putExtra(AuthCredentialsActivity.PARAM_PURPOSE, AuthCredentialsActivity.PURPOSE_REGISTER);
        startActivityForResult(credentialsIntent, REQUEST_AUTHENTICATE);
    }

}
