package org.syncloud.android.ui.apps;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.dialog.CommunicationDialog;
import org.syncloud.common.model.Result;
import org.syncloud.apps.owncloud.OwncloudManager;
import org.syncloud.ssh.model.Device;


public class Owncloud extends Activity {

    public static final String COM_OWNCLOUD_ANDROID = "com.owncloud.android";
    private Device device;
    private CommunicationDialog progress;
    private TextView url;
    private Button activateBtn;
    private Button webBtn;
    private Button mobileBtn;
    private LinearLayout activatedControls;
    private LinearLayout notActivatedControls;
    private EditText loginText;
    private EditText passText;
    private CheckBox chkHttps;
    private OwncloudManager owncloudManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_owncloud);

        SyncloudApplication application = (SyncloudApplication) getApplication();

        progress = new CommunicationDialog(this);

        owncloudManager = new OwncloudManager(application.createSsh());
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);

        activatedControls = (LinearLayout) findViewById(R.id.owncloud_activated_controls);
        notActivatedControls = (LinearLayout) findViewById(R.id.owncloud_not_activated_controls);
        url = (TextView) findViewById(R.id.owncloud_url);
        activateBtn = (Button) findViewById(R.id.owncloud_activate);

        webBtn = (Button) findViewById(R.id.owncloud_web_btn);
        mobileBtn = (Button) findViewById(R.id.owncloud_mobile_btn);

        loginText = (EditText) findViewById(R.id.txtLogin);
        passText = (EditText) findViewById(R.id.txtPassword);
        chkHttps = (CheckBox) findViewById(R.id.chkHttps);

        setVisibility(View.GONE, View.GONE);

        status();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.owncloud, menu);
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

    public void activate(View view) {
        final String login = loginText.getText().toString();
        final String pass = passText.getText().toString();
        final String protocol = chkHttps.isChecked() ? "https" : "http";

        new ProgressAsyncTask<String, String>()
                .setTitle("Activating ...")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<String, String>() {
                    @Override
                    public Result<String> run(String... args) {
                        Result<String> finishResult = owncloudManager.finishSetup(device, login, pass, protocol);
                        return finishResult;
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

    private void status() {
        new ProgressAsyncTask<Void, String>()
                .setTitle("Checking status")
                .setProgress(progress)
                .showError(false)
                .doWork(new ProgressAsyncTask.Work<Void, String>() {
                    @Override
                    public Result<String> run(Void... args) {
                        Result<String> result = owncloudManager.owncloudUrl(device);
                        return result;
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<String>() {
                    @Override
                    public void run(Result<String> result) {
                        if (!result.hasError()) {
                            url.setText(result.getValue());
                            setVisibility(View.VISIBLE, View.GONE);
                            progress.hide();
                        } else {
                            setVisibility(View.GONE, View.VISIBLE);
                            progress.hide();
                        }
                    }
                })
                .execute();
    }

    private void setVisibility(int activeControls, int inactiveControls) {
        activatedControls.setVisibility(activeControls);
        notActivatedControls.setVisibility(inactiveControls);
    }

    public void showWeb(View view) {
        CharSequence text = url.getText();
        if (text != null && !text.toString().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(text.toString()));
            startActivity(browserIntent);
        } else {
            progress.error("url is not set");
        }
    }

    public void showMobile(View view) {
        CharSequence text = url.getText();
        if (text != null && !text.toString().isEmpty()) {
            if (isInstalled(COM_OWNCLOUD_ANDROID)) {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(COM_OWNCLOUD_ANDROID);
                startActivity(LaunchIntent);
            } else {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                marketIntent.setData(Uri.parse("market://details?id=" + COM_OWNCLOUD_ANDROID));
                startActivity(marketIntent);
            }
        } else {
            progress.error("url is not set");
        }
    }

    public boolean isInstalled(String app)
    {
        try{
            getPackageManager().getApplicationInfo(app, 0);
            return true;
        } catch( PackageManager.NameNotFoundException e ){
            return false;
        }
    }

    public void copyUrl(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("url", url.getText());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progress.dismiss();
    }
}
