package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.DevicesSavedActivity;
import org.syncloud.android.db.Db;
import org.syncloud.ssh.model.Device;

public class DevicesSavedAdapter extends ArrayAdapter<Device> {
    private final Preferences preferences;
    private DevicesSavedActivity activity;

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_saved);
        this.activity = activity;
        preferences = ((SyncloudApplication) activity.getApplication()).getPreferences();
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

        TextView deviceName = (TextView) rowView.findViewById(R.id.device_host);
        LinearLayout deviceInfo = (LinearLayout) rowView.findViewById(R.id.device_info);
        TextView deviceLocalEndpoint = (TextView) rowView.findViewById(R.id.device_local_endpoint);
        TextView deviceRemoteEndpoint = (TextView) rowView.findViewById(R.id.device_remote_endpoint);
        ImageButton deviceRemove = (ImageButton) rowView.findViewById(R.id.device_remove);

        final Device device = getItem(position);
        deviceRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeSaved(device);
            }
        });
        deviceName.setText(device.getDisplayName());
        deviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.open(device);
            }
        });
        deviceLocalEndpoint.setText(device.getLocalEndpoint().host() + ":" + device.getLocalEndpoint().port());
        deviceRemoteEndpoint.setText(device.getUserDomain());

        deviceLocalEndpoint.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);
        deviceRemoteEndpoint.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);

        return rowView;

    }

}
