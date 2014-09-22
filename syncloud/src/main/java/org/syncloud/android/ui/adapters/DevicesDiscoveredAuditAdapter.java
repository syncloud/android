package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.discovery.Event;
import org.syncloud.android.ui.DevicesDiscoveryActivity;
import org.syncloud.android.ui.DevicesDiscoveryAuditActivity;
import org.syncloud.ssh.model.DirectEndpoint;

import java.text.SimpleDateFormat;

public class DevicesDiscoveredAuditAdapter extends ArrayAdapter<Event> {
    private DevicesDiscoveryAuditActivity activity;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DevicesDiscoveredAuditAdapter(DevicesDiscoveryAuditActivity activity) {
        super(activity, R.layout.layout_discovery_audit);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_discovery_audit, null);

        final Event event = getItem(position);

        TextView time = (TextView) rowView.findViewById(R.id.discovery_audit_date);
        time.setText(dateFormat.format(event.time));

        TextView type = (TextView) rowView.findViewById(R.id.discovery_audit_type);
        type.setText(event.type);

        return rowView;

    }
}
