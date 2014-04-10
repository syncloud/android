package org.syncloud.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

public class MainActivity extends Activity {

    private WifiManager.MulticastLock lock;
    private Updater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updater = new Updater() {
            @Override
            public void update(final String text) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView viewById = (TextView) findViewById(R.id.url);
                        viewById.setText(viewById.getText() + "\n" + text);
                    }
                });

            }
        };


        setUp();

        String type = "_http._tcp.local.";
        updater.update("service:" + type);
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... types) {
                startDiscovery(types[0]);
                return null;
            }
        }.execute(type);
    }

    private void startDiscovery(String type) {
        try {

            ServiceListener listener = new EventListener(updater);

            JmDNS jmdns = JmDNS.create();
            updater.update("jmdns created");
            jmdns.addServiceListener(type, listener);
            updater.update("jmdns listener added");
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private void setUp() { // to be called by onCreate
        android.net.wifi.WifiManager wifi =
                (android.net.wifi.WifiManager)
                        getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("HeeereDnssdLock");
        lock.setReferenceCounted(true);
        lock.acquire();

        updater.update("lock acquired");
    }
    protected void onDestroy() {
        if (lock != null) lock.release();
    }
}
