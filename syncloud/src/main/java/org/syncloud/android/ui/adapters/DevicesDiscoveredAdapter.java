package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ui.DevicesDiscoveryActivity;
import org.syncloud.platform.ssh.model.Identification;
import org.syncloud.platform.ssh.model.IdentifiedEndpoint;

public class DevicesDiscoveredAdapter extends ArrayAdapter<IdentifiedEndpoint> {
    private DevicesDiscoveryActivity activity;

    public DevicesDiscoveredAdapter(DevicesDiscoveryActivity activity) {
        super(activity, R.layout.layout_device_discovered);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_discovered, null);

        final IdentifiedEndpoint ie = getItem(position);

        TextView txtMain = (TextView) rowView.findViewById(R.id.txt_main_name);
        TextView txtHost = (TextView) rowView.findViewById(R.id.txt_host);
        TextView txtMacAddress = (TextView) rowView.findViewById(R.id.txt_second_line);

        LinearLayout layoutHost = (LinearLayout) rowView.findViewById(R.id.layout_host);
        LinearLayout layoutMacAddress = (LinearLayout) rowView.findViewById(R.id.layout_mac_address);
        TextView txtNoIdentification = (TextView) rowView.findViewById(R.id.txt_no_identification);

        txtHost.setText(ie.endpoint().host());

        if (ie.id().isPresent()) {
            Identification identification = ie.id().get();
            txtMain.setText(identification.title);

            txtNoIdentification.setVisibility(View.GONE);
            layoutHost.setVisibility(View.VISIBLE);
            layoutMacAddress.setVisibility(View.VISIBLE);

            txtHost.setText(ie.endpoint().host());
            txtMacAddress.setText(identification.mac_address);
        } else {
            txtMain.setText(ie.endpoint().host());

            txtNoIdentification.setVisibility(View.VISIBLE);
            layoutHost.setVisibility(View.GONE);
            layoutMacAddress.setVisibility(View.INVISIBLE);
        }

        return rowView;

    }
}
