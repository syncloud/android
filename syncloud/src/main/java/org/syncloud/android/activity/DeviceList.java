package org.syncloud.android.activity;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.discovery.Discovery;

import java.util.ArrayList;
import java.util.List;

public class DeviceList extends Activity {

    WifiManager.MulticastLock lock;
    public final static String MULTICAST_LOCK_TAG = DeviceList.class.toString();
    private ArrayAdapter<String> devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        final ListView listview = (ListView) findViewById(R.id.devices);
        devicesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        listview.setAdapter(devicesAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String url = (String) parent.getAdapter().getItem(position);
                Intent intent = new Intent(DeviceList.this, Device.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        discoverAsync().execute("ownCloud");

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

    private AsyncTask<String, Void, List<String>> discoverAsync() {
        return new AsyncTask<String, Void, List<String>>() {

            @Override
            protected List<String> doInBackground(String... input) {

                try {
                    WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
                    lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
                    lock.setReferenceCounted(true);
                    lock.acquire();
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    final int ip = connInfo.getIpAddress();

                    return Discovery.getUrl(ip, input[0]);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.release();
                }
                return new ArrayList<String>();
            }

            @Override
            protected void onPostExecute(List<String> urls) {
                TextView urlView = (TextView) findViewById(R.id.url);
                DeviceList.this.devicesAdapter.addAll(urls);
                if (urls.size() > 0) {
                    urlView.setText("done");
                } else {
                    urlView.setText("not found");

                }
            }

        };
    }

    public void rescan(View view) {
        TextView urlView = (TextView) findViewById(R.id.url);
        urlView.setText(R.string.searching_label);
        discoverAsync().execute();
    }



}
