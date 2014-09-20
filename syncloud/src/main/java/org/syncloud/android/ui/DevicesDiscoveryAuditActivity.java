package org.syncloud.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.android.discovery.Event;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAuditAdapter;
import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.ssh.model.DirectEndpoint;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DevicesDiscoveryAuditActivity extends Activity {

    private DevicesDiscoveredAuditAdapter adapter;
    private List<Event> discoveryEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_audit);
        final ListView listview = (ListView) findViewById(R.id.discovery_audit);
        adapter = new DevicesDiscoveredAuditAdapter(this);
        listview.setAdapter(adapter);
        discoveryEvents = ((SyncloudApplication) getApplication()).discoveryEvents;
        adapter.addAll(discoveryEvents);
    }

    public void refresh(View view) {
        adapter.clear();
        adapter.addAll(discoveryEvents);
    }
}
