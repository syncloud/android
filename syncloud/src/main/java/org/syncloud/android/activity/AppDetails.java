package org.syncloud.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.syncloud.android.R;

public class AppDetails extends Activity {

    private String app;
    private String deviceAddress;
    private TextView executeStatus;
    private Button installBtn;
    private Button removeBtn;
    private Button verifyBtn;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        TextView appName = (TextView) findViewById(R.id.app_name);
        executeStatus = (TextView) findViewById(R.id.execute_status);
        installBtn = (Button) findViewById(R.id.install_btn);
        removeBtn = (Button) findViewById(R.id.remove_btn);
        verifyBtn = (Button) findViewById(R.id.verify_btn);

        appName.setText(getIntent().getExtras().getString("app_name"));
        app = getIntent().getExtras().getString("app");
        deviceAddress = getIntent().getExtras().getString("device_address");
        progress = new ProgressDialog(this);
        progress.setMessage("Talking to the device");
        progress.show();
//        status(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_details, menu);
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



/*
    public void status(final boolean append) {

        new AsyncTask<String, String, Result<InstallStatus>>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected Result<InstallStatus> doInBackground(String... strings) {
                try {
                    return Result.value(Spm.status(strings[0], strings[1]));
                } catch (final Exception e) {
                    return Result.error(e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(Result<InstallStatus> status) {
                if (!status.hasError()) {
                    if (status.getValue().isInstalled()) {
                        installBtn.setVisibility(View.GONE);
                        removeBtn.setVisibility(View.VISIBLE);
                        verifyBtn.setVisibility(View.VISIBLE);
                    } else {
                        installBtn.setVisibility(View.VISIBLE);
                        removeBtn.setVisibility(View.GONE);
                        verifyBtn.setVisibility(View.GONE);
                    }

                    String message = "Status: " + status.getValue().getMessage();
                    if (append) {
                        executeStatus.setText(executeStatus.getText() + "\n" + message);
                    } else {
                        executeStatus.setText(message);
                    }
                } else {
                    executeStatus.setText(status.getError());
                }
                progress.hide();
            }
        }.execute(deviceAddress, app);

    }
*/

    /*public void remove(View view) {

        new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                Result<String> result = Spm.run(Spm.Commnand.Remove, strings[0], strings[1]);
                return result.hasError() ? result.getError() : result.getValue();
            }

            @Override
            protected void onPostExecute(String status) {
                executeStatus.setText(status);
                status(true);
            }
        }.execute(deviceAddress, app);

    }*/

    public void update(View view) {
    }

    /*public void verify(View view) {

        new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    return Spm.verify(strings[0], strings[1]).getMessage();
                } catch (final Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String status) {
                executeStatus.setText(status);
                status(true);
            }
        }.execute(deviceAddress, app);

    }*/
}
