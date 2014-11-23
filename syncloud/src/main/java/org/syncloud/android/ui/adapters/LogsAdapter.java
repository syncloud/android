package org.syncloud.android.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.common.model.LogEvent;
import org.syncloud.android.ui.LogsActivity;

import java.text.SimpleDateFormat;

public class LogsAdapter extends ArrayAdapter<LogEvent> {
    private LogsActivity activity;

    private Boolean showTime = false;
    private Boolean showLevel = false;

    public LogsAdapter(LogsActivity activity) {
        super(activity, R.layout.layout_log);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_log, null);

        if (position % 2 == 0)
            rowView.setBackgroundColor(Color.parseColor("#FFE4E4E4"));

        final LogEvent event = getItem(position);

        String message = "";
        if (showTime)
            message += event.getTimestamp() + " ";
        if (showLevel)
            message += event.getLevel() + " " + event.getTag() + " ";

        message += event.getMessage();

        TextView text = (TextView) rowView.findViewById(R.id.log_message);
        text.setText(message);

        return rowView;
    }

    public void toggleTime() {
        showTime = !showTime;
    }

    public void toggleLevel() {
        showLevel = !showLevel;
    }
}