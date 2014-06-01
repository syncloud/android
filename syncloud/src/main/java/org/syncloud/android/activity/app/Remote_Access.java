package org.syncloud.android.activity.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.common.base.Optional;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.db.Db;
import org.syncloud.app.RemoteAccessManager;
import org.syncloud.model.Device;
import org.syncloud.model.Result;

import static android.os.AsyncTask.execute;

public class Remote_Access extends Activity {

    private ProgressDialog progress;
    private Switch remoteAccessSwitch;
    private Device device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_remote_access);
        progress = new ProgressDialog(this);
        progress.setMessage("Talking to the device");
        device = (Device) getIntent().getSerializableExtra(SyncloudApplication.DEVICE);
        remoteAccessSwitch = (Switch) findViewById(R.id.remote_access);
        status();

    }

    private void enable() {

        progress.setMessage("Enabling");
        progress.show();

        execute(new Runnable() {
            @Override
            public void run() {
                final Result<Device> result = RemoteAccessManager.enable(device);
                if (result.hasError()) {
                    showError(result.getError());
                    return;
                }

                final Db db = ((SyncloudApplication) getApplication()).getDb();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        db.insert(result.getValue());
                        progress.hide();
                    }
                });
            }
        });

    }

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(error);
                progress.setCancelable(true);
            }
        });
    }

    private void status() {

        progress.setMessage("Checking status");
        progress.show();

        execute(new Runnable() {
            @Override
            public void run() {

                final Result<Optional<Device>> remoteDevice = RemoteAccessManager.getRemoteDevice(device);
                if (remoteDevice.hasError()){
                    showError(remoteDevice.getError());
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remoteAccessSwitch.setOnCheckedChangeListener(null);
                        remoteAccessSwitch.setChecked(remoteDevice.getValue().isPresent());
                        remoteAccessSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                                if (on) {
                                    enable();
                                } else {
                                    disable();
                                }
                            }
                        });
                        progress.hide();
                        }
                    });
            }
        });
    }

    private void disable() {

        progress.show();
        progress.setMessage("Disabling");

        execute(new Runnable() {
            @Override
            public void run() {

                final Result<Optional<Device>> remoteDevice = RemoteAccessManager.getRemoteDevice(device);
                if (remoteDevice.hasError()){
                    showError(remoteDevice.getError());
                    return;
                }

                if (!remoteDevice.getValue().isPresent()){
                    showError("not enabled");
                    return;
                }

                Result<Boolean> disabled = RemoteAccessManager.disable(device);

                if (disabled.hasError()) {
                    showError(disabled.getError());
                    return;
                }

                if (!disabled.getValue()) {
                    showError("Unable to disable");
                    return;
                }

                final Db db = ((SyncloudApplication) getApplication()).getDb();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        db.remove(remoteDevice.getValue().get());
                        progress.hide();
                    }
                });

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.device_settings, menu);
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
