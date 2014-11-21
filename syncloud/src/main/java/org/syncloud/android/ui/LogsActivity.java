package org.syncloud.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.syncloud.android.R;
import org.syncloud.android.log.Logcat;
import org.syncloud.android.ui.adapters.LogsAdapter;

public class LogsActivity extends Activity {

    private LogsAdapter adapter;
    private Logcat logcat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        final ListView listview = (ListView) findViewById(R.id.logs_list);
        adapter = new LogsAdapter(this);
        listview.setAdapter(adapter);
        logcat = new Logcat(adapter);
        logcat.readLog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refresh(View view) {
        logcat.readLog();
    }
}
