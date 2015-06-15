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

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.Utils;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DevicesSavedAdapter;
import org.syncloud.redirect.IUserService;
import org.syncloud.redirect.model.User;
import org.syncloud.platform.ssh.model.DomainModel;
import org.syncloud.platform.ssh.model.Key;

import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.sort;

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
                DomainModel domain = (DomainModel)obj;
                if (domain.hasDevice())
                    open(domain);
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
        final IUserService userService = application.userServiceCached();
        final String email = preferences.getEmail();
        final String password = preferences.getPassword();

        new ProgressAsyncTask<Void, User>()
                .doWork(new ProgressAsyncTask.Work<Void, User>() {
                    @Override
                    public User run(Void... args) {
                        return userService.getUser(email, password);
                    }
                })
                .onSuccess(new ProgressAsyncTask.Success<User>() {
                    @Override
                    public void run(User user) {
                        updateUser(user);
                    }
                })
                .execute();
    }

    private void updateUser(User user) {
        List<Key> keys = keysStorage.list();
        List<DomainModel> domains = Utils.toDevices(user.domains, keys);

        Comparator<DomainModel> noDevicesLast = new Comparator<DomainModel>() {
            @Override
            public int compare(DomainModel first, DomainModel second) {
                if (!second.hasDevice() && first.hasDevice())
                    return -1;
                if (second.hasDevice() && !first.hasDevice())
                    return 1;
                return 0;
            }
        };

        sort(domains, noDevicesLast);

        adapter.clear();
        for (DomainModel domain : domains)
            adapter.add(domain);
    }


    private void open(DomainModel device) {
//        Intent intent = new Intent(this, DeviceAppsActivity.class);
//        intent.putExtra(SyncloudApplication.DOMAIN, device);

        Intent intent = new Intent(this, DeviceWebView.class);

        //TODO: We need remote/local selector here
        String url = format(
                "http://%s/server/rest/login?" +
                        "name=%s&" +
                        "password=%s",
                device.device().localEndpoint().host(),
                device.device().credentials().login(),
                device.device().credentials().password());
        intent.putExtra(SyncloudApplication.DEVICE_URL, url);
        intent.putExtra(SyncloudApplication.DEVICE_CREDENTIALS, device.device().credentials());
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

}
