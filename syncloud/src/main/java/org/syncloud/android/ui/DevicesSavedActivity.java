package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.Utils;
import org.syncloud.android.db.Db;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.ui.adapters.DevicesSavedAdapter;
import org.syncloud.redirect.IUserCache;
import org.syncloud.redirect.model.User;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Key;

import java.util.List;

public class DevicesSavedActivity extends Activity {

    private Db db;
    private KeysStorage keysStorage;
    private IUserCache userCache;
    private DevicesSavedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_devices_saved);
        final ListView listview = (ListView) findViewById(R.id.devices_saved);
        listview.setEmptyView(findViewById(android.R.id.empty));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Object obj = listview.getItemAtPosition(position);
                Device device = (Device)obj;
                open(device);
            }
        });

        adapter = new DevicesSavedAdapter(this);
        listview.setAdapter(adapter);

        SyncloudApplication application = (SyncloudApplication) getApplication();
//        db = application.getDb();
        keysStorage = application.keysStorage();
        userCache = application.userCache();

        refreshDevices();
    }

    private void refreshDevices() {
        try {
            User user = userCache.load();
            List<Key> keys = keysStorage.list();
            List<Device> devices = Utils.toDevices(user.domains, keys);

            adapter.clear();
//        List<Device> devices = db.list();
            for (Device device : devices)
                adapter.add(device);
        } catch (Exception ex) {

        }
    }

    private void open(Device device) {
        Intent intent = new Intent(this, DeviceAppsActivity.class);
        intent.putExtra(SyncloudApplication.DEVICE, device);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshDevices();
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
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        }
        return super.onOptionsItemSelected(item);
    }

    public void discover(View view) {
        startActivityForResult(new Intent(this, DevicesDiscoveryActivity.class), 1);
    }

    public void shareDevice(Device device) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, device.userDomain());
        String body = "";
        body += "Host: " + device.userDomain() + "\n";
        body += "Login: " + device.credentials().login() + "\n";
        body += "Password: " + device.credentials().password() + "\n";
        body += "KEY:\n\n" + device.credentials().key() + "\n";
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
