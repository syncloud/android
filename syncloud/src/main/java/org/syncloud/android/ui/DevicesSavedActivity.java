package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.Utils;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.ui.adapters.DevicesSavedAdapter;
import org.syncloud.redirect.IUserService;
import org.syncloud.redirect.UserResult;
import org.syncloud.redirect.model.User;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.Key;

import java.util.List;

public class DevicesSavedActivity extends Activity {

    private KeysStorage keysStorage;
    private DevicesSavedAdapter adapter;
    private SyncloudApplication application;
    private Preferences preferences;


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

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();

        keysStorage = application.keysStorage();

        refreshDevices();
    }

    private void refreshDevices() {
        new CheckCredentialsTask(application, preferences).execute();
    }

    private void updateUser(User user) {
        List<Key> keys = keysStorage.list();
        List<Device> devices = Utils.toDevices(user.domains, keys);

        adapter.clear();
        for (Device device : devices)
            adapter.add(device);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public class CheckCredentialsTask extends AsyncTask<Void, Void, UserResult> {
        private Preferences preferences;
        private IUserService userService;

        public CheckCredentialsTask(SyncloudApplication application, Preferences preferences) {
            this.preferences = preferences;
            this.userService = application.userService();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected UserResult doInBackground(Void... voids) {
            String email = preferences.getEmail();
            String password = preferences.getPassword();
            UserResult result = userService.getUser(email, password);
            return result;
        }

        @Override
        protected void onPostExecute(UserResult result) {
            if (!result.hasError()) {
                updateUser(result.user());
            }
        }
    }

}
