package org.syncloud.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.activity.DevicesDiscoveryActivity;
import org.syncloud.android.activity.DevicesSavedActivity;
import org.syncloud.android.db.Db;
import org.syncloud.model.Device;

public class DevicesDiscoveredAdapter extends ArrayAdapter<Device> {
    private DevicesDiscoveryActivity activity;

    public DevicesDiscoveredAdapter(DevicesDiscoveryActivity activity) {
        super(activity, R.layout.layout_device_discovered);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_discovered, null);

        final Device device = getItem(position);

        ImageButton deviceAdd = (ImageButton) rowView.findViewById(R.id.device_add);
        deviceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.open(device);
            }
        });

        TextView deviceHost = (TextView) rowView.findViewById(R.id.device_discovered_host);
        deviceHost.setText(device.getHost() + ":" + device.getPort());

        return rowView;

    }
}
