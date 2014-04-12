package org.syncloud.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.syncloud.discovery.Discovery;

public class MainActivity extends Activity {

    WifiManager.MulticastLock lock;
    Handler handler = new android.os.Handler();
    public final static String MULTICAST_LOCK_TAG = MainActivity.class.toString();

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
                } else {
                    urlView.setText("not found");
                }
            }

        }.execute();


    }
}
