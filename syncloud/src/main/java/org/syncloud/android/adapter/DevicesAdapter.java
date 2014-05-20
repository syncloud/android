package org.syncloud.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.activity.DeviceListActivity;
import org.syncloud.android.db.DeviceDb;
import org.syncloud.model.Device;

public class DevicesAdapter extends ArrayAdapter<Device> {
    private final DeviceDb deviceDb;
    private DeviceListActivity activity;

    public DevicesAdapter(DeviceListActivity activity) {
        super(activity, R.layout.layout_app);
        this.activity = activity;
        deviceDb = new DeviceDb(activity);
        for (Device device : deviceDb.list()) {
            add(device);
        }
    }

    public void removeSaved(Device device) {
        deviceDb.remove(device);
        remove(device);
    }

    public void save(Device device) {
        if (!deviceDb.list().contains(device))
            deviceDb.insert(device);
        add(device);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device, null);
        TextView deviceHost = (TextView) rowView.findViewById(R.id.device_host);
        TextView deviceRemove = (TextView) rowView.findViewById(R.id.device_remove);
        final Device device = getItem(position);
        if (device.getKey() == null) {
            deviceRemove.setVisibility(View.GONE);
        }
        deviceRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(device);
            }
        });
        deviceHost.setText(device.getHost() + ":" + device.getPort());
        deviceHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.open(device);
            }
        });

        return rowView;

    }
}
