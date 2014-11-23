package org.syncloud.android.log;

import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import org.syncloud.android.ui.adapters.LogsAdapter;
import org.syncloud.common.LogParser;
import org.syncloud.common.model.LogEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logcat {

    private LogsAdapter adapter;
    private String TAG = Logcat.class.getSimpleName();

    private LogParser parser = new LogParser();

    public Logcat(LogsAdapter adapter) {
        this.adapter = adapter;
    }

    public void readLog() {
        adapter.clear();
        final int pid = android.os.Process.myPid();
        try {
            final BufferedReader reader = execute();

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    reader.close();
                    break;
                }
                Optional<LogEvent> eventOpt = parser.parse(line);
                if (eventOpt.isPresent()) {
                    LogEvent event = eventOpt.get();
                    if (event.getPid() == pid) {
                        adapter.add(event);
                    }
                }
            }

        } catch (IOException e) {
            Log.d(TAG, "unable to run logcat: " + e.getMessage());

        }
    }

    private BufferedReader execute() throws IOException {
        final List<String> commandLine = commandLine();
        String[] args = commandLine.toArray(new String[commandLine.size()]);
        Log.d(TAG, Joiner.on(" ").join(commandLine));
        final Process process = Runtime.getRuntime().exec(args);
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    private List<String> commandLine() {
        final List<String> commandLine = new ArrayList<String>();

        commandLine.add("logcat");
        commandLine.add("-v");
        commandLine.add("time");
        commandLine.add("-t");
        commandLine.add("100");
        commandLine.add("-b");
        commandLine.add("main");
        commandLine.add("*:D");
        return commandLine;
    }

}
