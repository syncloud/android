package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.Tools;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;
import org.syncloud.ssh.model.IdentifiedEndpoint;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;
import static org.syncloud.ssh.model.Credentials.getStandardCredentials;

public class DevicesDiscoveryActivity extends Activity {
    private Preferences preferences;

    private AsyncDiscovery asyncDiscovery;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private Button refreshBtn;
    private ProgressBar progressBar;
    private DevicesDiscoveredAdapter listAdapter;

    private LinearLayout layoutResults;
    private LinearLayout layoutNoWifi;

    private ListView resultsList;

    private Map<Endpoint, IdentifiedEndpoint> map;
    private Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyncloudApplication application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        tools = new Tools(new SshRunner());
        setContentView(R.layout.activity_devices_discovery);

        layoutResults = (LinearLayout) findViewById(R.id.layout_results);
        layoutNoWifi = (LinearLayout) findViewById(R.id.layout_no_wifi);
        resultsList = (ListView) findViewById(R.id.devices_discovered);
        progressBar = (ProgressBar) findViewById(R.id.discovery_progress);
        refreshBtn = (Button) findViewById(R.id.discovery_refresh_btn);
        listAdapter = new DevicesDiscoveredAdapter(this);
        resultsList.setAdapter(listAdapter);

        map = newHashMap();

        DeviceEndpointListener deviceEndpointListener = new DeviceEndpointListener() {
            @Override
            public void added(final Endpoint endpoint) {
                Result<Identification> idResult = tools.getId(endpoint, getStandardCredentials());
                Identification id = null;
                if (!idResult.hasError())
                    id = idResult.getValue();
                final IdentifiedEndpoint ie = new IdentifiedEndpoint(endpoint, id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.put(endpoint, ie);
                        listAdapter.add(ie);
                    }
                });
            }

            @Override
            public void removed(final Endpoint endpoint) {
                final IdentifiedEndpoint ie = map.remove(endpoint);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.remove(ie);
                    }
                });
            }
        };

        asyncDiscovery = new AsyncDiscovery(
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                (NsdManager) getSystemService(Context.NSD_SERVICE),
                deviceEndpointListener);

        discoveryStart();
    }

    private boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    private void discoveryStart() {
        listAdapter.clear();
        if (isWifiConnected()) {
            layoutResults.setVisibility(View.VISIBLE);
            layoutNoWifi.setVisibility(View.GONE);
            refreshBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            asyncDiscovery.start(preferences.getDiscoveryLibrary());
            //        use for testing without wi-fi
            //        listAdapter.add(new DirectEndpoint("localhost", 22, "vsapronov", "somepassword", null));
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            discoveryStop();
                        }
                    });
                }
            }, 20, TimeUnit.SECONDS);
        } else {
            layoutResults.setVisibility(View.GONE);
            layoutNoWifi.setVisibility(View.VISIBLE);
        }
    }

    private void discoveryStop() {
        asyncDiscovery.stop();
        progressBar.setVisibility(View.INVISIBLE);
        refreshBtn.setEnabled(true);
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
            startActivityForResult(new Intent(this, SettingsActivity.class), 1);
        }
        return super.onOptionsItemSelected(item);
    }

    public void open(final IdentifiedEndpoint endpoint) {
        Intent intent = new Intent(this, DeviceActivateActivity.class);
        intent.putExtra(SyncloudApplication.DEVICE_ENDPOINT, endpoint);
        startActivity(intent);
        setResult(Activity.RESULT_OK, new Intent(this, DevicesSavedActivity.class));
        finish();
    }

    public void openWiFiSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asyncDiscovery.stop();
    }

    public void refresh(View view) {
        discoveryStart();
    }
}
