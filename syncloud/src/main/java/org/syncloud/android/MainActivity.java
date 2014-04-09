package org.syncloud.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class MainActivity extends ActionBarActivity {

    private NsdManager.DiscoveryListener discoveryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /*discoveryListener = new NsdManager.DiscoveryListener() {
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

                *//*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*//*

//                Log.d("syncloud", "Service resolved: " + info.getServiceName() + " host:" + info.getHost() + " port:"
//                        + info.getPort() + " type:" + info.getServiceType());

                NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {

                    }

                    @Override
                    public void onServiceResolved(final NsdServiceInfo nsdServiceInfo) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                url.setText(nsdServiceInfo.getServiceName() + " on " + nsdServiceInfo.getHost() + ":" + nsdServiceInfo.getPort());
                            }
                        });


//                        Log.d("syncloud", "Service resolved: " + info.getServiceName() + " host:" + info.getHost() + " port:"
//                                + info.getPort() + " type:" + info.getServiceType());

                            mNsdManager.stopServiceDiscovery(discoveryListener);


                    }
                };
                if (info.getServiceName().equals("ownCloud"))
                    mNsdManager.resolveService(info, resolveListener);

            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {

            }
        };*/


        AsyncTask<String, Void, Void> asyncTask = new AsyncTask<String, Void, Void>() {

            ServiceListener listener = new ServiceListener() {

                @Override
                public void serviceAdded(final ServiceEvent event) {
                    if (event.getName().equals("ownCloud")) {
                        Log.d("s", "add: " + event.getType());
                        Log.d("s", "add: " + event.getInfo().getPort());
                        Log.d("s", "add: " + event.getInfo().getServer());

                        final TextView url = (TextView) findViewById(R.id.url);

                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                url.setText(event.getType());
                            }
                        });*/

                        final ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());

                        Log.d("s", "more: " + info.getType());
//                        Log.d("s", "more: " + info.getInet4Addresses()[0].getHostAddress());

                        String server = info.getServer();
                        String local = ".local.";
                        if (server.endsWith(local))
                            server = server.substring(0, server.length() - local.length());

                        final String text = "URL: http://" + server + ":" + info.getPort() + info.getNiceTextString();

                        Log.d("s", "text: " + text);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                url.setText(text);
                            }
                        });

                    }

                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    Log.d("s", "removed: " + event.getType() + "" + event.getName());

                }

                @Override
                public void serviceResolved(ServiceEvent event) {
                    if (event.getName().equals("ownCloud")) {
                        Log.d("s", "resolved: " + event.getType());
                        Log.d("s", "resolved: " + event.getInfo().getPort());
                        Log.d("s", "resolved: " + event.getInfo().getServer());
                    }
                }

            };

            @Override
            protected Void doInBackground(String... voids) {
                JmDNS jmdns = null;
                try {
                    jmdns = JmDNS.create();
                    jmdns.addServiceListener("_http._tcp.local.", listener);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        asyncTask.execute("");


        /*new Thread(new Runnable() {
            @Override
            public void run() {
                final NsdManager mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

//                mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener);

            }
        }).start();*/

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
