package org.syncloud.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.common.eventbus.Subscribe;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.discovery.Event;
import org.syncloud.android.discovery.EventCache;
import org.syncloud.android.ui.adapters.DevicesDiscoveredAuditAdapter;

public class DevicesDiscoveryAuditActivity extends Activity {

    private DevicesDiscoveredAuditAdapter adapter;
    private EventCache discoveryEventCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_audit);
        final ListView listview = (ListView) findViewById(R.id.discovery_audit);
        adapter = new DevicesDiscoveredAuditAdapter(this);
        listview.setAdapter(adapter);
        SyncloudApplication application = (SyncloudApplication) getApplication();
        application.eventbus.register(this);
        discoveryEventCache = application.discoveryEventCache;
        init();
    }

    @Subscribe
    public void add(final Event event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(event);
            }
        });
    }

    private void init() {
        adapter.clear();
        adapter.addAll(discoveryEventCache.events);
    }
}
