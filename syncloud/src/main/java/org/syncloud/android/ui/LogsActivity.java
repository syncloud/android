package org.syncloud.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.syncloud.android.R;
import org.syncloud.android.log.LogEvent;
import org.syncloud.android.ui.adapters.LogsAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogsActivity extends Activity {

    private LogsAdapter adapter;
    private List<LogEvent> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        final ListView listview = (ListView) findViewById(R.id.logs_list);
        adapter = new LogsAdapter(this);
        listview.setAdapter(adapter);

        readLog();

    }

    private void readLog() {
        final List<String> commandLine = new ArrayList<String>();
        final int myPid = android.os.Process.myPid();
        String myPidStr = Integer.toString(myPid) +"):";

        commandLine.add("logcat");
        commandLine.add("-t");
        commandLine.add("100");
        commandLine.add("-b");
        commandLine.add("main");
//        commandLine.add("-v");
//        commandLine.add("long");
        commandLine.add("*:D");
        try {
            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                if (line.contains(myPidStr))
                    adapter.add(new LogEvent(new Date(), "", line));
            }

        } catch (IOException e) {
            Log.d("syncloud.logcat", "unable to run logcat: " + e.getMessage());

        }
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
        adapter.clear();
        readLog();
    }
}
