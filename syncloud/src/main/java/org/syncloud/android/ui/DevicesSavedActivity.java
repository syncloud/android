package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.Progress;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.Utils;
import org.syncloud.android.tasks.AsyncResult;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DevicesSavedAdapter;
import org.syncloud.android.core.redirect.IUserService;
import org.syncloud.android.core.redirect.model.User;
import org.syncloud.android.core.platform.model.DomainModel;
import org.syncloud.android.core.platform.model.Key;

import java.util.Comparator;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static org.syncloud.android.network.Helpers.findAccessibleUrl;

public class DevicesSavedActivity extends Activity {

    private static Logger logger = Logger.getLogger(DevicesSavedActivity.class);

    private DevicesSavedAdapter adapter;
    private SyncloudApplication application;
    private Preferences preferences;
    private ProgressBar progressBar;

    private Progress progress = new ProgressImpl();

    public class ProgressImpl extends Progress.Empty {
        @Override
        public void start() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void stop() {
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void error(String message) {

        }
    }


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
                DomainModel domain = (DomainModel) obj;
                if (domain.hasDevice())
                    open(domain);
            }
        });

        adapter = new DevicesSavedAdapter(this);
        listview.setAdapter(adapter);

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        progressBar = (ProgressBar) findViewById(R.id.open_progress);

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
        List<DomainModel> domains = Utils.toDevices(user.domains);

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


    private void open(final DomainModel device) {
        new ProgressAsyncTask<Void, Optional<String>>()
                .setTitle("Activating device")
                .setProgress(progress)
                .doWork(new ProgressAsyncTask.Work<Void, Optional<String>>() {
                    @Override
                    public Optional<String> run(Void... args) {
                        return findAccessibleUrl(device);
                    }
                })
                .onCompleted(new ProgressAsyncTask.Completed<Optional<String>>() {
                    @Override
                    public void run(AsyncResult<Optional<String>> result) {
                        onOpenDevice(result);
                    }
                })
                .execute();
    }

    private void onOpenDevice(AsyncResult<Optional<String>> result) {
        if (!result.hasValue()) {
            logger.error("unable to connect ");
            showError();
            return;
        }

        Optional<String> baseUrl = result.getValue();

        if (!baseUrl.isPresent()) {
            logger.error("unable to connect ");
            showError();
            return;
        }

        String url = format("%s/server/html/login.html", baseUrl.get());

//            Optional<Credentials> credentials = device.device().credentials();
//            if (credentials.isPresent()) {
//                String postData = "name=" + credentials.get().login() + "&" + "password=" + credentials.get().password();
//            }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showError() {
        new AlertDialog.Builder(this)
                .setTitle("Can't connect to the device")
                .setMessage("Device is not reachable or it is set to internal mode only.")
                .setPositiveButton("OK", null)
                .show();
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
