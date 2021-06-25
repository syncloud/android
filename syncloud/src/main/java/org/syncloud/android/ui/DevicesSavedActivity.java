package org.syncloud.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.syncloud.android.Preferences;
import org.syncloud.android.Progress;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.Utils;
import org.syncloud.android.core.platform.model.DomainModel;
import org.syncloud.android.core.redirect.IUserService;
import org.syncloud.android.core.redirect.model.User;
import org.syncloud.android.tasks.ProgressAsyncTask;
import org.syncloud.android.ui.adapters.DevicesSavedAdapter;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;

public class DevicesSavedActivity extends AppCompatActivity {

    private ListView listview;
    private DevicesSavedAdapter adapter;
    private SyncloudApplication application;
    private Preferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton btnDiscovery;
    private View emptyView;

    private final Progress progress = new ProgressImpl();

    public class ProgressImpl extends Progress.Empty {
        @Override
        public void start() {
            swipeRefreshLayout.setRefreshing(true);
            listview.setEnabled(false);
            btnDiscovery.setVisibility(View.GONE);
        }

        @Override
        public void stop() {
            swipeRefreshLayout.setRefreshing(false);
            listview.setEnabled(true);
            btnDiscovery.setVisibility(View.VISIBLE);
        }

        @Override
        public void error(String message) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_devices_saved);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        emptyView = findViewById(android.R.id.empty);

        listview = findViewById(R.id.devices_saved);
        listview.setOnItemClickListener((adapterView, view, position, l) -> {
            Object obj = listview.getItemAtPosition(position);
            DomainModel domain = (DomainModel) obj;
            open(domain);
        });

        btnDiscovery = findViewById(R.id.discovery_btn);

        adapter = new DevicesSavedAdapter(this);
        listview.setAdapter(adapter);

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green);
        swipeRefreshLayout.setOnRefreshListener(this::refreshDevices);

        swipeRefreshLayout.post(this::refreshDevices);
    }

    private void refreshDevices() {
        final IUserService userService = application.userServiceCached();
        final String email = preferences.getRedirectEmail();
        final String password = preferences.getRedirectPassword();

        emptyView.setVisibility(View.GONE);
        listview.setEmptyView(null);
        adapter.clear();

        new ProgressAsyncTask<Void, User>()
                .setProgress(progress)
                .doWork(args -> userService.getUser(email, password))
                .onCompleted(result -> {
                    emptyView.setVisibility(View.VISIBLE);
                    listview.setEmptyView(emptyView);
                })
                .onSuccess(this::updateUser)
                .execute();
    }

    private void updateUser(User user) {
        List<DomainModel> domains = Utils.toModels(user.domains);

        Comparator<DomainModel> noDevicesLast = (first, second) -> first.name().compareTo(second.name());

        sort(domains, noDevicesLast);

        adapter.clear();
        for (DomainModel domain : domains)
            adapter.add(domain);
    }


    private void open(final DomainModel device) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(device.getDnsUrl()));
        startActivity(browserIntent);
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
