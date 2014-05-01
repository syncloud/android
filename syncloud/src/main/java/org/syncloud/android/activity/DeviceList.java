package org.syncloud.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.syncloud.android.R;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.discovery.DeviceListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeviceList extends Activity {

    private ArrayAdapter<String> devicesAdapter;
    private AsyncDiscovery asyncDiscovery;
    private Set<String> devices = new HashSet<String>();

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

        DeviceListener deviceListener = new DeviceListener() {
            @Override
            public void added(final String url) {
                if (!devices.contains(url)) {
                    devices.add(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devicesAdapter.add(url);
                        }
                    });
                }
            }

            @Override
            public void removed(final String url) {
                if (devices.contains(url)) {
                    devices.remove(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { devicesAdapter.remove(url);
                        }
                    });
                }
            }
        };

        asyncDiscovery = new AsyncDiscovery(
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                deviceListener);
        ((ToggleButton) findViewById(R.id.discoveryToggle)).setChecked(true);
        asyncDiscovery.start();

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

    public void onDiscoveryToggle(View view) {
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            asyncDiscovery.start();
        } else {
            asyncDiscovery.stop();
        }

    }
}
