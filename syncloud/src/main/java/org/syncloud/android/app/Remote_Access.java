package org.syncloud.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.syncloud.android.R;
import org.syncloud.model.Result;
import org.syncloud.model.PortMapping;
import org.syncloud.model.SshResult;
import org.syncloud.integration.ssh.Insider;

import java.util.List;

public class Remote_Access extends Activity {

    public static final int REMOTE_ACCESS_PORT = 1022;
    private ProgressDialog progress;
    private Switch remoteAccessSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_access);
        progress = new ProgressDialog(this);
        progress.setMessage("Talking to the device");
        final String address = getIntent().getExtras().getString("device_address" );
        remoteAccessSwitch = (Switch) findViewById(R.id.remote_access);
        remoteAccessSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                if (on) {
                    asyncEnable(address);
                } else {
                    asyncDisable(address);
                }
            }
        });
        asyncStatus(address);

    }

    private void asyncEnable(String address) {
        new AsyncTask<String, String, Result<SshResult>>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected Result<SshResult> doInBackground(String... strings) {
                return Insider.addPort(strings[0], REMOTE_ACCESS_PORT);
                //TODO: Need to:
                //TODO: 1. Generate root ssh key
                //TODO: 2. Bookmark this device using name/port/key
//                Ssh.execute("");
            }

            @Override
            protected void onPostExecute(Result<SshResult> result) {
                progress.hide();

                if (result.hasError()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Remote_Access.this);
                    builder.setMessage(result.getError());
                    remoteAccessSwitch.setChecked(false);
                }

            }
        }.execute(address);
    }

    private void asyncStatus(String address) {
        new AsyncTask<String, String, Result<List<PortMapping>>>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected Result<List<PortMapping>> doInBackground(String... strings) {
                return Insider.listPortMappings(strings[0]);
            }

            @Override
            protected void onPostExecute(Result<List<PortMapping>> result) {

                remoteAccessSwitch.setChecked(!result.hasError() && result.getValue().contains(new PortMapping(REMOTE_ACCESS_PORT)));


                if (result.hasError()) {
                    progress.setMessage(result.getError());
                    progress.setCancelable(true);
                } else {
                    progress.hide();
                }

            }
        }.execute(address);
    }

    private void asyncDisable(String address) {
        new AsyncTask<String, String, Result<SshResult>>() {
            @Override
            protected void onPreExecute() {
                progress.show();
            }

            @Override
            protected Result<SshResult> doInBackground(String... strings) {
                return Insider.removePort(strings[0], REMOTE_ACCESS_PORT);
            }

            @Override
            protected void onPostExecute(Result<SshResult> result) {
                progress.hide();

                if (result.hasError()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Remote_Access.this);
                    builder.setMessage(result.getError());
                    remoteAccessSwitch.setChecked(true);
                }

            }
        }.execute(address);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device_settings, menu);
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
}
