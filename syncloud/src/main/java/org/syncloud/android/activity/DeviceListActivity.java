package org.syncloud.android.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.adapter.DevicesAdapter;
import org.syncloud.android.config.Params;
import org.syncloud.android.R;
import org.syncloud.android.db.DeviceDb;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.discovery.DeviceListener;
import org.syncloud.model.Device;

import java.util.HashSet;
import java.util.Set;

public class DeviceListActivity extends Activity {

    private DevicesAdapter devicesAdapter;
    private AsyncDiscovery asyncDiscovery;
    private Set<org.syncloud.model.Device> devices = new HashSet<org.syncloud.model.Device>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        final ListView listview = (ListView) findViewById(R.id.devices);
        devicesAdapter = new DevicesAdapter(this);
        ((SyncloudApplication)getApplication()).setDevicesAdapter(devicesAdapter);

        listview.setAdapter(devicesAdapter);

        DeviceListener deviceListener = new DeviceListener() {
            @Override
            public void added(final org.syncloud.model.Device device) {
                if (!devices.contains(device)) {
                    devices.add(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devicesAdapter.add(device);
                        }
                    });
                }
            }

            @Override
            public void removed(final org.syncloud.model.Device device) {
                if (devices.contains(device)) {
                    devices.remove(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devicesAdapter.remove(device);
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

    public void open(Device device) {
        Intent intent = new Intent(DeviceListActivity.this, DeviceActivity.class);
        intent.putExtra(Params.DEVICE, device);
        startActivity(intent);
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
