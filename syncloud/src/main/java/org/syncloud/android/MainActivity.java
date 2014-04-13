package org.syncloud.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.syncloud.android.activation.Owncloud;
import org.syncloud.android.activation.Result;
import org.syncloud.discovery.Discovery;

public class MainActivity extends Activity {

    WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = MainActivity.class.toString();
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findSyncloudDevice();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    public void openEbay(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ebay.com"));
        startActivity(browserIntent);
    }


    private void findSyncloudDevice() {


        discoverAsync().execute();


    }

    private AsyncTask<Void, Void, Optional<String>> discoverAsync() {
        return new AsyncTask<Void, Void, Optional<String>>() {

            @Override
            protected Optional<String> doInBackground(Void... voids) {

                try {
                    WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(true);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    final int ip = connInfo.getIpAddress();

                    return Discovery.getUrl(ip, "ownCloud");

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.release();
                }
                return Optional.absent();
            }

            @Override
            protected void onPostExecute(Optional<String> url) {
                TextView urlView = (TextView) findViewById(R.id.url);
                if (url.isPresent()) {
                    urlView.setText(url.get());
                    MainActivity.this.url = url.get();
                } else {
                    urlView.setText("not found");
                }
            }

        };
    }

    public void activate(View view) {

        EditText loginText = (EditText) findViewById(R.id.login);
        EditText passText = (EditText) findViewById(R.id.pass);
        //TODO: Some validation

        String login = loginText.getText().toString();
        String pass = passText.getText().toString();


        finishSetupAsync().execute(login, pass);

    }

    private AsyncTask<String, Void, Result<String>> finishSetupAsync() {
        return new AsyncTask<String, Void, Result<String>>() {

            @Override
            protected void onPreExecute() {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText("activating ...");
            }

            @Override
            protected Result<String> doInBackground(String... input) {
                return Owncloud.finishSetup(url, input[0], input[1]);
            }

            @Override
            protected void onPostExecute(Result<String> result) {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText(result.hasError() ? result.getError() : result.getValue());
            }
        };
    }

    public void rescan(View view) {

        TextView urlView = (TextView) findViewById(R.id.url);
        urlView.setText(R.string.searching_label);
        findSyncloudDevice();
    }

    public void activateName(View view) {

        EditText loginText = (EditText) findViewById(R.id.name_login);
        EditText passText = (EditText) findViewById(R.id.name_pass);
        EditText emailText = (EditText) findViewById(R.id.name_email);

        EditText owncloudUserText = (EditText) findViewById(R.id.login);
        EditText owncloudPasswordText = (EditText) findViewById(R.id.pass);
        //TODO: Some validation

        String login = loginText.getText().toString();
        String pass = passText.getText().toString();
        String email = emailText.getText().toString();
        String owncloudUser = owncloudUserText.getText().toString();
        String owncloudPassword = owncloudPasswordText.getText().toString();

        TextView status = (TextView) findViewById(R.id.name_status);
        boolean valid = true;
        if (login.matches("")){
            status.setText("enter name login");
            valid = false;
        }

        if (pass.matches("")) {
            status.setText("enter name password");
            valid = false;
        }
       /* if (email.matches(""))
            status.setText("enter login");*/
        if (owncloudUser.matches("")) {
            status.setText("enter device login");
            valid = false;
        }
        if (owncloudPassword.matches("")) {
            status.setText("enter device password");
            valid = false;
        }

        if (valid)
            activateNameAsync().execute(login, pass, email, owncloudUser, owncloudPassword);
    }

    private AsyncTask<String, Void, Result<String>> activateNameAsync() {
        return new AsyncTask<String, Void, Result<String>>() {

            @Override
            protected void onPreExecute() {
                TextView status = (TextView) findViewById(R.id.name_status);
                status.setText("activating name ...");
            }

            @Override
            protected Result<String> doInBackground(String... input) {
                return Owncloud.activateName(url, input[0], input[1], input[2], input[3], input[4]);
            }

            @Override
            protected void onPostExecute(Result<String> result) {
                TextView status = (TextView) findViewById(R.id.name_status);
                status.setText(result.hasError() ? result.getError() : result.getValue());
            }
        };
    }
}
