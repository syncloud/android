package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.discovery.DiscoveryManager;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.Tools;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;
import org.syncloud.ssh.model.IdentifiedEndpoint;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.syncloud.ssh.model.Credentials.getStandardCredentials;

public class DevicesDiscoveryActivity extends Activity {

    private static Logger logger = LogManager.getLogger(DevicesDiscoveryActivity.class.getName());

    private Preferences preferences;

    private DiscoveryManager discoveryManager;
    private Button refreshBtn;
    private ProgressBar progressBar;
    private DevicesDiscoveredAdapter listAdapter;

    private LinearLayout layoutResults;
    private LinearLayout layoutNoWifi;

    private ListView resultsList;

    private Map<Endpoint, IdentifiedEndpoint> map;
    private Tools tools;
    private Boolean discoveryInProgress = false;


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

        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Object obj = resultsList.getItemAtPosition(position);
                IdentifiedEndpoint ie = (IdentifiedEndpoint)obj;
                open(ie);
            }
        });

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

        discoveryManager = new DiscoveryManager(
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
            if (!discoveryInProgress) {
                new DiscoveryTask().execute(preferences.getDiscoveryLibrary());
            }
        } else {
            layoutResults.setVisibility(View.GONE);
            layoutNoWifi.setVisibility(View.VISIBLE);
        }
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

    private void open(final IdentifiedEndpoint endpoint) {
        if (endpoint.id() == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Can't identify device")
                    .setMessage("Sorry, there's no identification information for this device. Most probably it is running old release of Syncloud. Please upgrade it to latest release and try to activate again.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Intent intent = new Intent(this, DeviceActivateActivity.class);
            intent.putExtra(SyncloudApplication.DEVICE_ENDPOINT, endpoint);
            startActivity(intent);
            setResult(Activity.RESULT_OK, new Intent(this, DevicesSavedActivity.class));
            finish();
        }
    }

    public void openWiFiSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.info("leaving the screen");
        if (discoveryInProgress)
            discoveryManager.cancel();
    }

    public void refresh(View view) {
        discoveryStart();
    }

    public class DiscoveryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            discoveryInProgress = true;
            layoutResults.setVisibility(View.VISIBLE);
            layoutNoWifi.setVisibility(View.GONE);
            refreshBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            //use for testing without wi-fi
            //listAdapter.add(new DirectEndpoint("localhost", 22, "vsapronov", "somepassword", null));

        }

        @Override
        protected Void doInBackground(String... libraries) {
            discoveryManager.run(libraries[0], 20);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            logger.info("show controls");
            progressBar.setVisibility(View.INVISIBLE);
            refreshBtn.setEnabled(true);
            discoveryInProgress = false;
        }
    }
}
