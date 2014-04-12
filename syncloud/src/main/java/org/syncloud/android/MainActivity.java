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


        new AsyncTask<Void, Void, Optional<String>>() {

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

        }.execute();


    }

    public void activate(View view) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText("activating ...");
            }

            @Override
            protected Boolean doInBackground(Void... voids) {

                EditText login = (EditText) findViewById(R.id.login);
                EditText pass = (EditText) findViewById(R.id.pass);
                //TODO: Some validation

                return Owncloud.finishSetup(url, login.getText().toString(), pass.getText().toString());
            }

            @Override
            protected void onPostExecute(Boolean activated) {
                TextView status = (TextView) findViewById(R.id.status);
                status.setText(activated ? "activated" : "not activated");
            }
        }.execute();

    }

    public void rescan(View view) {

        TextView urlView = (TextView) findViewById(R.id.url);
        urlView.setText(R.string.searching_label);
        findSyncloudDevice();
    }
}
