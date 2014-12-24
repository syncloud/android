package org.syncloud.android.ui.apps;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.apps.gitbucket.GitBucketManager;
import org.syncloud.ssh.model.Device;

public class GitBucket extends ActionBarActivity {

    private Device device;
    private CommunicationDialog progress;
    private GitBucketManager manager;

    private LinearLayout activatedControls;
    private TextView txtUrl;

    private LinearLayout notActivatedControls;
    private EditText loginText;
    private EditText passText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_gitbucket);

        SyncloudApplication application = (SyncloudApplication) getApplication();

        progress = new CommunicationDialog(this);
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);
        manager = new GitBucketManager(application.createSsh());

        activatedControls = (LinearLayout) findViewById(R.id.activated_controls);
        txtUrl = (TextView) findViewById(R.id.txt_url);

        notActivatedControls = (LinearLayout) findViewById(R.id.not_activated_controls);
        loginText = (EditText) findViewById(R.id.txt_login);
        passText = (EditText) findViewById(R.id.txt_password);

        activatedControls.setVisibility(View.GONE);
        notActivatedControls.setVisibility(View.GONE);

        status();
    }

    private void status() {
        new ProgressAsyncTask<Void, String>()
                .setTitle("Checking status")
                .setProgress(progress)
                .showError(false)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public AsyncResult<String> run(Void... args) {
                        return new AsyncResult<String>(
                                manager.url(device),
                                "unable to get GitBucket txtUrl");
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<String>() {
                    @Override
                    public void run(AsyncResult<String> result) {
                        if (!result.hasError()) {
                            txtUrl.setText(result.getValue());
                            setActive(true);
                        } else {
                            setActive(false);
                        }
                    }
                })
                .execute();
    }

    private boolean activateValidate(String login, String pass) {
        boolean valid = true;

        if (TextUtils.isEmpty(pass)) {
            passText.setError(getString(R.string.error_field_required));
            passText.requestFocus();
            valid = false;
        }
        if (TextUtils.isEmpty(login)) {
            loginText.setError(getString(R.string.error_field_required));
            loginText.requestFocus();
            valid = false;
        }
        return valid;
    }

    public void activate(View view) {
        final String login = loginText.getText().toString();
        final String pass = passText.getText().toString();

        if (!activateValidate(login, pass)) return;

        new ProgressAsyncTask<String, String>()
                .setTitle("Activating ...")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<String, String>() {
                    @Override
                    public AsyncResult<String> run(String... args) {
                        return new AsyncResult<String>(
                                manager.enable(device, login, pass),
                                "unable to activate");
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<String>() {
                    @Override
                    public void run(String result) {
                        status();
                    }
                })
                .execute();
    }

    public void deactivate(View view) {
        new ProgressAsyncTask<String, String>()
                .setTitle("Deactivating ...")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<String, String>() {
                    @Override
                    public AsyncResult<String> run(String... args) {
                        return new AsyncResult<String>(
                                manager.disable(device),
                                "unable to deactivate");
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<String>() {
                    @Override
                    public void run(String result) {
                        status();
                    }
                })
                .execute();
    }

    private void setActive(boolean value) {
        int activatedVisibility = value ? View.VISIBLE : View.GONE;
        int nonActivatedVisibility = value ? View.GONE : View.VISIBLE;

        activatedControls.setVisibility(activatedVisibility);
        notActivatedControls.setVisibility(nonActivatedVisibility);
    }

    public void showWeb(View view) {
        CharSequence text = txtUrl.getText();
        if (text != null && !text.toString().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text.toString()));
            startActivity(browserIntent);
        }
    }

    public void copyUrl(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("txtUrl", txtUrl.getText());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }
}
