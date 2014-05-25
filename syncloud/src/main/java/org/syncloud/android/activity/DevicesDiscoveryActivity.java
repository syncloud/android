package org.syncloud.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.adapter.DevicesDiscoveredAdapter;
import org.syncloud.android.db.Db;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.app.RemoteAccessManager;
import org.syncloud.discovery.DeviceListener;
import org.syncloud.model.Device;
import org.syncloud.model.Result;

import static android.os.AsyncTask.execute;

public class DevicesDiscoveryActivity extends Activity {

    private AsyncDiscovery asyncDiscovery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_discovery);
        final ListView listview = (ListView) findViewById(R.id.devices_discovered);
        final DevicesDiscoveredAdapter listAdapter = new DevicesDiscoveredAdapter(this);
        listview.setAdapter(listAdapter);

        DeviceListener deviceListener = new DeviceListener() {
            @Override
            public void added(final org.syncloud.model.Device device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.add(device);
                    }
                });
            }

            @Override
            public void removed(final org.syncloud.model.Device device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.remove(device);
                    }
                });
            }
        };


        asyncDiscovery = new AsyncDiscovery(
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                deviceListener);
        asyncDiscovery.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.discovery, menu);
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

    public void open(final Device device) {
        Intent intent = new Intent(this, DeviceActivateActivity.class);
        intent.putExtra(SyncloudApplication.DEVICE, device);
        startActivity(intent);
        setResult(Activity.RESULT_OK, new Intent(this, DevicesSavedActivity.class));
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        asyncDiscovery.stop();
    }
}
