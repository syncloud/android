package org.syncloud.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.core.platform.model.Identification;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.android.discovery.DiscoveryManager;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter;
import org.syncloud.android.ui.dialog.WifiDialog;
import org.syncloud.android.core.platform.Internal;
import org.syncloud.android.core.platform.model.Endpoint;
import org.syncloud.android.core.platform.model.IdentifiedEndpoint;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class DevicesDiscoveryActivity extends AppCompatActivity {

    private static Logger logger = Logger.getLogger(DevicesDiscoveryActivity.class.getName());

    private Preferences preferences;

    private DiscoveryManager discoveryManager;
    private FloatingActionButton refreshBtn;
    private DevicesDiscoveredAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    private ListView resultsList;

    private Map<Endpoint, IdentifiedEndpoint> map;
    private Internal internal;
    private SyncloudApplication application;

    private static int REQUEST_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_devices_discovery);

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        internal = new Internal();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkWiFiAndDiscover();
            }
        });

        emptyView = findViewById(android.R.id.empty);

        resultsList = (ListView) findViewById(R.id.devices_discovered);

        refreshBtn = (FloatingActionButton) findViewById(R.id.discovery_refresh_btn);
        listAdapter = new DevicesDiscoveredAdapter(this);
        resultsList.setAdapter(listAdapter);

        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Object obj = resultsList.getItemAtPosition(position);
                IdentifiedEndpoint ie = (IdentifiedEndpoint) obj;
                open(ie);
            }
        });

        map = newHashMap();

        discoveryManager = new DiscoveryManager(
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                (NsdManager) getSystemService(Context.NSD_SERVICE));

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                checkWiFiAndDiscover();
            }
        });
    }

    private void checkWiFiAndDiscover() {
        listAdapter.clear();
        if (application.isWifiConnected()) {
            new DiscoveryTask().execute();
        } else {
            WifiDialog dialog = new WifiDialog();
            dialog.setMessage("Discovery is possible only in the same Wi-Fi network where you have Syncloud device connected.");
            dialog.show(getSupportFragmentManager(), "discovery_wifi_dialog");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WifiDialog.WIFI_SETTINGS) {
            checkWiFiAndDiscover();
        } else {
            finish();
        }
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
            Intent intentSettings = new Intent(this, SettingsActivity.class);
            startActivityForResult(intentSettings, REQUEST_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    private void open(final IdentifiedEndpoint endpoint) {
        if (!endpoint.id().isPresent()) {
            new AlertDialog.Builder(this)
                    .setTitle("Can't identify device")
                    .setMessage("Sorry, there's no identification information for this device. Most probably it is running old release of Syncloud. Please upgrade it to latest release and try to activate again.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(endpoint.endpoint().activationUrl()));
            startActivity(browserIntent);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.info("leaving the screen");
        discoveryManager.cancel();
    }

    public void refresh(View view) {
        checkWiFiAndDiscover();
    }

    public class DiscoveryTask extends AsyncTask<Void, Progress, Void> {

        private final DeviceEndpointListener deviceEndpointListener;

        public DiscoveryTask() {
            deviceEndpointListener = new DeviceEndpointListener() {
                @Override
                public void added(final Endpoint endpoint) {
                    Optional<Identification> id = internal.getId(endpoint.host());
                    if (id.isPresent()) {
                        IdentifiedEndpoint ie = new IdentifiedEndpoint(endpoint, id);
                        publishProgress(new Progress(true, endpoint, ie));
                    }
                }

                @Override
                public void removed(final Endpoint endpoint) {
                    IdentifiedEndpoint ie = map.remove(endpoint);
                    publishProgress(new Progress(false, endpoint, ie));
                }
            };
        }

        @Override
        protected void onPreExecute() {
            refreshBtn.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
            emptyView.setVisibility(View.GONE);
            resultsList.setEmptyView(null);
            listAdapter.clear();

            //use for testing without wi-fi
            //listAdapter.add(new DirectEndpoint("localhost", 22, "vsapronov", "somepassword", null));
        }

        @Override
        protected Void doInBackground(Void... params) {
            discoveryManager.run(20, deviceEndpointListener);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            emptyView.setVisibility(View.VISIBLE);
            resultsList.setEmptyView(emptyView);
            swipeRefreshLayout.setRefreshing(false);
            refreshBtn.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Progress... progresses) {
            Progress progress = progresses[0];
            IdentifiedEndpoint ie = progress.identifiedEndpoint;
            if (progress.isAdded) {
                map.put(progress.endpoint, ie);
                listAdapter.add(ie);
            } else {
                listAdapter.remove(ie);
            }
        }
    }

    public class Progress {
        boolean isAdded = true;
        Endpoint endpoint;
        IdentifiedEndpoint identifiedEndpoint;

        public Progress(boolean isAdded, Endpoint endpoint, IdentifiedEndpoint identifiedEndpoint) {
            this.isAdded = isAdded;
            this.endpoint = endpoint;
            this.identifiedEndpoint = identifiedEndpoint;
        }
    }
}
