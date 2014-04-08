package org.syncloud.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private NsdManager.DiscoveryListener discoveryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final NsdManager mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);


        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {

            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {

            }

            @Override
            public void onDiscoveryStarted(String s) {

            }

            @Override
            public void onDiscoveryStopped(String s) {

            }

            @Override
            public void onServiceFound(final NsdServiceInfo info) {

                final TextView url = (TextView) findViewById(R.id.url);
//                url.setText(info.getServiceName() + " on " + info.getHost() + ":" + info.getPort());

                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/

//                Log.d("syncloud", "Service resolved: " + info.getServiceName() + " host:" + info.getHost() + " port:"
//                        + info.getPort() + " type:" + info.getServiceType());

                NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {

                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {

                        url.setText(info.getServiceName() + " on " + info.getHost() + ":" + info.getPort());

                        Log.d("syncloud", "Service resolved: " + info.getServiceName() + " host:" + info.getHost() + " port:"
                                + info.getPort() + " type:" + info.getServiceType());

                            mNsdManager.stopServiceDiscovery(discoveryListener);


                    }
                };
                if (info.getServiceName().equals("ownCloud"))
                    mNsdManager.resolveService(info, resolveListener);

            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {

            }
        };

        mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener);



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

}
