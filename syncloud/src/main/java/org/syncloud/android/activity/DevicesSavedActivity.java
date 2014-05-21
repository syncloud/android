package org.syncloud.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.adapter.DevicesSavedAdapter;
import org.syncloud.model.Device;

public class DevicesSavedActivity extends Activity {


    private DevicesSavedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_saved);
        final ListView listview = (ListView) findViewById(R.id.devices_saved);
        adapter = new DevicesSavedAdapter(this);
        listview.setAdapter(adapter);
        adapter.refresh();
    }

    public void open(Device device) {
        Intent intent = new Intent(this, DeviceAppsActivity.class);
        intent.putExtra(SyncloudApplication.DEVICE, device);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.refresh();
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

    public void discover(View view) {
        startActivityForResult(new Intent(this, DevicesDiscoveryActivity.class), 1);
    }
}
