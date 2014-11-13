package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.android.discovery.DeviceEndpointListener;
import org.syncloud.ssh.model.DirectEndpoint;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DevicesDiscoveryActivity extends Activity {
    private Preferences preferences;

    private AsyncDiscovery asyncDiscovery;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private Button refreshBtn;
    private ProgressBar progressBar;
    private DevicesDiscoveredAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = ((SyncloudApplication)getApplication()).getPreferences();

        setContentView(R.layout.activity_devices_discovery);
        final ListView listview = (ListView) findViewById(R.id.devices_discovered);
        progressBar = (ProgressBar) findViewById(R.id.discovery_progress);
        refreshBtn = (Button) findViewById(R.id.discovery_refresh_btn);
        listAdapter = new DevicesDiscoveredAdapter(this);
        listview.setAdapter(listAdapter);

        DeviceEndpointListener deviceEndpointListener = new DeviceEndpointListener() {
            @Override
            public void added(final DirectEndpoint endpoint) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.add(endpoint);
                    }
                });
            }

            @Override
            public void removed(final DirectEndpoint endpoint) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.remove(endpoint);
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

    private void discoveryStart() {
        listAdapter.clear();
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
        }, 20, TimeUnit.SECONDS );
    }

    private void discoveryStop() {
        asyncDiscovery.stop();
        progressBar.setVisibility(View.GONE);
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

    public void open(final DirectEndpoint endpoint) {
        Intent intent = new Intent(this, DeviceActivateActivity.class);
        intent.putExtra(SyncloudApplication.DEVICE_ENDPOINT, endpoint);
        startActivity(intent);
        setResult(Activity.RESULT_OK, new Intent(this, DevicesSavedActivity.class));
        finish();
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
