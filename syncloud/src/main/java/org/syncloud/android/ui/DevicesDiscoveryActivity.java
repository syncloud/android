package org.syncloud.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.log4j.Logger;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.android.discovery.DiscoveryManager;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter;
import org.syncloud.android.ui.dialog.WifiDialog;
import org.syncloud.common.WebService;
import org.syncloud.platform.ssh.Tools;
import org.syncloud.platform.ssh.model.Endpoint;
import org.syncloud.platform.ssh.model.IdentifiedEndpoint;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;

public class DevicesDiscoveryActivity extends FragmentActivity {

    private static Logger logger = Logger.getLogger(DevicesDiscoveryActivity.class.getName());

    private Preferences preferences;

    private DiscoveryManager discoveryManager;
    private Button refreshBtn;
    private ProgressBar progressBar;
    private DevicesDiscoveredAdapter listAdapter;

    private ListView resultsList;

    private Map<Endpoint, IdentifiedEndpoint> map;
    private Tools tools;
    private SyncloudApplication application;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (SyncloudApplication) getApplication();
        preferences = application.getPreferences();
        tools = new Tools(new WebService());
        setContentView(R.layout.activity_devices_discovery);

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

        discoveryManager = new DiscoveryManager(
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                (NsdManager) getSystemService(Context.NSD_SERVICE));

        checkWiFiAndDiscover();
    }

    private void checkWiFiAndDiscover() {
        listAdapter.clear();
        if (application.isWifiConnected()) {
            new DiscoveryTask().execute(preferences.getDiscoveryLibrary());
        } else {
            WifiDialog dialog = new WifiDialog();
            dialog.setMessage("Discovery is possible only in the same Wi-Fi network where you have Syncloud device connected.");
            dialog.show(getSupportFragmentManager(), "discovery_wifi_dialog");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==WifiDialog.WIFI_SETTINGS)
        {
            checkWiFiAndDiscover();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discovery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 1);
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

            /*Intent intent = new Intent(this, DeviceActivateActivity.class);
            intent.putExtra(SyncloudApplication.DEVICE_ENDPOINT, endpoint.endpoint());
            intent.putExtra(SyncloudApplication.DEVICE_ID, endpoint.id().get());
            startActivity(intent);
            setResult(Activity.RESULT_OK, new Intent(this, DevicesSavedActivity.class));*/

            Intent intent = new Intent(this, DeviceWebView.class);
            intent.putExtra(SyncloudApplication.DEVICE_ID, endpoint.id().get());
            String url = format(
                    "http://%s:81/server/html/activate.html?" +
                            "redirect-email=%s&" +
                            "redirect-password=%s&" +
                            "release=%s",
                    endpoint.endpoint().host(),
                    preferences.getEmail(),
                    preferences.getPassword(),
                    preferences.getVersion());
            intent.putExtra(SyncloudApplication.DEVICE_URL, url);

            startActivity(intent);
            setResult(Activity.RESULT_OK, new Intent(this, DeviceWebView.class));

            finish();
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

    public class DiscoveryTask extends AsyncTask<String, Progress, Void> {

        private final DeviceEndpointListener deviceEndpointListener;

        public DiscoveryTask() {
            deviceEndpointListener = new DeviceEndpointListener() {
                @Override
                public void added(final Endpoint endpoint) {
                    IdentifiedEndpoint ie = new IdentifiedEndpoint(endpoint, tools.getId(endpoint.host()));
                    publishProgress(new Progress(true, endpoint, ie));
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
            refreshBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            //use for testing without wi-fi
            //listAdapter.add(new DirectEndpoint("localhost", 22, "vsapronov", "somepassword", null));
        }

        @Override
        protected Void doInBackground(String... libraries) {
            discoveryManager.run(libraries[0], 20, deviceEndpointListener);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            logger.info("show controls");
            progressBar.setVisibility(View.INVISIBLE);
            refreshBtn.setEnabled(true);
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
