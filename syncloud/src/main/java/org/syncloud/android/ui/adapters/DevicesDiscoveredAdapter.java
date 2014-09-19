package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ui.DevicesDiscoveryActivity;
import org.syncloud.ssh.model.DirectEndpoint;

public class DevicesDiscoveredAdapter extends ArrayAdapter<DirectEndpoint> {
    private DevicesDiscoveryActivity activity;

    public DevicesDiscoveredAdapter(DevicesDiscoveryActivity activity) {
        super(activity, R.layout.layout_device_discovered);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_discovered, null);

        final DirectEndpoint endpoint = getItem(position);

        ImageButton deviceAdd = (ImageButton) rowView.findViewById(R.id.device_add);
        deviceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.open(endpoint);
            }
        });

        TextView deviceHost = (TextView) rowView.findViewById(R.id.device_discovered_host);
        deviceHost.setText(endpoint.getDisplayName());

        return rowView;

    }
}
