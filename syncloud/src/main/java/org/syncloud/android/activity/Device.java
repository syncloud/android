package org.syncloud.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.app.Repo;
import org.syncloud.model.App;

import java.util.List;


public class Device extends Activity {

    private String address;
    private Repo repo = new Repo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        TextView deviceAddress = (TextView) findViewById(R.id.device_address);
        address = getIntent().getExtras().getString("address");
        deviceAddress.setText(address);

        final ListView listview = (ListView) findViewById(R.id.app_list);
        final ArrayAdapter<App> appsAdapter = new ArrayAdapter<App>(this,
                android.R.layout.simple_list_item_1);
        listview.setAdapter(appsAdapter);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final List<App> apps = repo.list();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appsAdapter.addAll(apps);
                    }
                });
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                App app = (App) parent.getAdapter().getItem(position);
                Intent intent = new Intent(Device.this, AppDetails.class);
                intent.putExtra("device_address", address);
                intent.putExtra("app_name", app.getName());
                intent.putExtra("app_url", repo.getUrl());
                intent.putExtra("app", app.getId());
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(Device.this, DeviceSettings.class);
            intent.putExtra("device_address", address);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void dns(View view) {
        Intent intent = new Intent(this, DnsActivity.class);
        intent.putExtra("address", address);
        startActivity(intent);
    }

    public void owncloud(View view) {
        Intent intent = new Intent(this, OwncloudActivity.class);
        intent.putExtra("address", address);
        startActivity(intent);
    }
}
