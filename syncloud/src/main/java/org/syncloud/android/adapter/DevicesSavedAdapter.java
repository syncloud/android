package org.syncloud.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.activity.DevicesSavedActivity;
import org.syncloud.android.db.Db;
import org.syncloud.model.Device;

public class DevicesSavedAdapter extends ArrayAdapter<Device> {
    private DevicesSavedActivity activity;

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_saved);
        this.activity = activity;
    }

    private Db db() {
        return  ((SyncloudApplication) activity.getApplication()).getDb();
    }

    public void refresh() {
        clear();
        for (Device device : db().list()) {
            add(device);
        }
    }

    public void removeSaved(Device device) {
        db().remove(device);
        remove(device);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_saved, null);
        TextView deviceHost = (TextView) rowView.findViewById(R.id.device_host);
        ImageButton deviceRemove = (ImageButton) rowView.findViewById(R.id.device_remove);
        final Device device = getItem(position);
        deviceRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSaved(device);
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
