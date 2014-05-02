package org.syncloud.android.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ssh.Ssh;

public class AppDetails extends Activity {

    private String appScript;
    private String deviceAddress;
    private TextView executeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        TextView appNmae = (TextView) findViewById(R.id.app_name);
        executeStatus = (TextView) findViewById(R.id.execute_status);

        appNmae.setText(getIntent().getExtras().getString("app_name"));
        appScript = getIntent().getExtras().getString("app_script");
        deviceAddress = getIntent().getExtras().getString("device_address");

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

    public void install(View view) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String status;
                try {
                    status = Ssh.install(deviceAddress, appScript);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(status);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(e.getMessage());
                        }
                    });
                }

            }
        });
    }

    public void remove(View view) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String status;
                try {
                    status = Ssh.remove(deviceAddress, appScript);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(status);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(e.getMessage());
                        }
                    });
                }

            }
        });
    }
}
