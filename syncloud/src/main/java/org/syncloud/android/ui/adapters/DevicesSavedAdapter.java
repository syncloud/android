package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.DevicesSavedActivity;
import org.syncloud.ssh.model.DomainModel;

public class DevicesSavedAdapter extends ArrayAdapter<DomainModel> {
    private final Preferences preferences;
    private DevicesSavedActivity activity;

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_saved);
        this.activity = activity;
        preferences = ((SyncloudApplication) activity.getApplication()).getPreferences();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_saved, null);

        TextView txtDeviceTitle = (TextView) rowView.findViewById(R.id.txt_device_title);
        TextView txtDomainName = (TextView) rowView.findViewById(R.id.txt_domain_name);
        TextView txtMacAddress = (TextView) rowView.findViewById(R.id.txt_mac_address);

        final DomainModel domain = getItem(position);

        txtDomainName.setText(domain.userDomain());
        txtDeviceTitle.setText(domain.device().id().title());
        txtMacAddress.setText(domain.device().id().macAddress());

        txtMacAddress.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);

        return rowView;
    }

}
