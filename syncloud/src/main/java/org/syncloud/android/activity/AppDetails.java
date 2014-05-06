package org.syncloud.android.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.model.InstallStatus;
import org.syncloud.android.model.VerifyStatus;
import org.syncloud.android.ssh.Ssh;

public class AppDetails extends Activity {

    private String app;
    private String deviceAddress;
    private TextView executeStatus;
    private Button installBtn;
    private Button removeBtn;
    private Button verifyBtn;

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

        status(false);
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
                    status = Ssh.install(deviceAddress, app);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(status);
                            status(true);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(e.getMessage());
                            status(true);
                        }
                    });
                }

            }
        });
    }

    public void status(final boolean append) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final InstallStatus status = Ssh.status(deviceAddress, app);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status.isInstalled()) {
                                installBtn.setVisibility(View.GONE);
                                removeBtn.setVisibility(View.VISIBLE);
                                verifyBtn.setVisibility(View.VISIBLE);
                            } else {
                                installBtn.setVisibility(View.VISIBLE);
                                removeBtn.setVisibility(View.GONE);
                                verifyBtn.setVisibility(View.GONE);
                            }
                            String message = "Status: " + status.getMessage();
                            if (append) {
                                executeStatus.setText(executeStatus.getText() + "\n" + message);
                            } else {
                                executeStatus.setText(message);
                            }
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
                    status = Ssh.remove(deviceAddress, app);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(status);
                            status(true);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(e.getMessage());
                            status(true);
                        }
                    });
                }

            }
        });
    }

    public void update(View view) {
    }

    public void verify(View view) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final VerifyStatus status;
                try {
                    status = Ssh.verify(deviceAddress, app);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            executeStatus.setText(status.getMessage());
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
