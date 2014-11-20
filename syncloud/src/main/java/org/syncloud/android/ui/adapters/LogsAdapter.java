package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.log.LogEvent;
import org.syncloud.android.ui.LogsActivity;

import java.text.SimpleDateFormat;

public class LogsAdapter extends ArrayAdapter<LogEvent> {
    private LogsActivity activity;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogsAdapter(LogsActivity activity) {
        super(activity, R.layout.layout_log);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_log, null);
        final LogEvent event = getItem(position);
        TextView time = (TextView) rowView.findViewById(R.id.log_date);
        time.setText(dateFormat.format(event.getDate()));
        TextView level = (TextView) rowView.findViewById(R.id.log_level);
        level.setText(event.getLevel());
        TextView message = (TextView) rowView.findViewById(R.id.log_message);
        message.setText(event.getMessage());
        return rowView;
    }
}