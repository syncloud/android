package org.syncloud.android.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.syncloud.android.Params;
import org.syncloud.android.R;
import org.syncloud.app.InsiderManager;
import org.syncloud.model.InsiderConfig;
import org.syncloud.model.InsiderDnsConfig;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;

import java.util.List;


public class Insider extends Activity {

    private String device;
    private ProgressDialog progress;
    private TextView managedDomain;
    private TextView userDomainName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insider);
        progress = new ProgressDialog(this);

        managedDomain = (TextView) findViewById(R.id.managed_domain);
        userDomainName = (TextView) findViewById(R.id.user_domain_name);

        device = getIntent().getExtras().getString(Params.DEVICE_ADDRESS);

        status(device);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dns, menu);
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

    private void showError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setMessage(error);
                progress.setCancelable(true);
            }
        });
    }

    private void status(final String device) {

        progress.setMessage("Checking name status ...");
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Result<InsiderConfig> config = InsiderManager.config(device);

                if (config.hasError()) {
                    showError(config.getError());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            managedDomain.setText(config.getValue().getDomain());
                        }
                    });

                    final Result<Optional<InsiderDnsConfig>> dnsConfig = InsiderManager.dnsConfig(device);
                    if (dnsConfig.hasError()) {
                        showError(dnsConfig.getError());
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Optional<InsiderDnsConfig> dnsConfigs = dnsConfig.getValue();
                                if (dnsConfigs.isPresent())
                                    userDomainName.setText(dnsConfigs.get().getUser_domain());
                                else
                                    userDomainName.setText("");
                                progress.hide();
                            }
                        });

                    }
                }
            }
        });
    }

    public void activateName(View view) {

        EditText emailText = (EditText) findViewById(R.id.name_email);
        EditText passText = (EditText) findViewById(R.id.name_pass);
        EditText userDomainText = (EditText) findViewById(R.id.user_domain);

        String email = emailText.getText().toString();
        String pass = passText.getText().toString();
        String domain = userDomainText.getText().toString();

        TextView status = (TextView) findViewById(R.id.dns_status);
        boolean valid = true;

        if (email.matches("")) {
            status.setText("enter name password");
            valid = false;
        }

        if (pass.matches("")) {
            status.setText("enter name password");
            valid = false;
        }

        /*if (domain.matches("")){
            status.setText("enter name domain name");
            valid = false;
        }*/


        if (valid) {
            if (domain.matches(""))
                existingName(device, email, pass);
            else
                newName(device, email, pass, domain);
        }
    }

    private void newName(final String device, final String email, final String pass, final String domain) {

        progress.setMessage("Activating new name ...");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Result<SshResult> result = InsiderManager.newName(device, email, pass, domain);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.hasError()) {
                            progress.setMessage(result.getError());
                            progress.setCancelable(true);
                        } else {
                            progress.hide();
                        }
                    }
                });
            }
        });
    }

    private void existingName(final String device, final String email, final String pass) {

        progress.setMessage("Activating existing name ...");
        progress.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Result<SshResult> result = InsiderManager.activateExistingName(device, email, pass);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.hasError()) {
                            progress.setMessage(result.getError());
                            progress.setCancelable(true);
                        } else {
                            progress.hide();
                            status(device);
                        }
                    }
                });
            }
        });
    }
}
