package org.syncloud.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.syncloud.android.activity.DeviceList;
import org.syncloud.model.Device;

public class DevicesAdapter extends ArrayAdapter<Device> {
    private DeviceList activity;

    public DevicesAdapter(DeviceList activity) {
        super(activity, R.layout.layout_app);
        this.activity = activity;
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
                activity.remove(device);
            }
        });
        deviceHost.setText(device.getIp());
        deviceHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.open(device);
            }
        });

        return rowView;

    }
}
