package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ui.DevicesDiscoveryActivity;
import org.syncloud.android.core.platform.model.IdentifiedEndpoint;

public class DevicesDiscoveredAdapter extends ArrayAdapter<IdentifiedEndpoint> {
    private DevicesDiscoveryActivity activity;

    public DevicesDiscoveredAdapter(DevicesDiscoveryActivity activity) {
        super(activity, R.layout.layout_device_item);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_item, null);

        TextView txtBoldTitle = (TextView) rowView.findViewById(R.id.txt_bold_title);
        TextView txtAdditionalLine = (TextView) rowView.findViewById(R.id.txt_additional_line);

        final IdentifiedEndpoint ie = getItem(position);

        txtBoldTitle.setText(ie.id().get().title);
        txtAdditionalLine.setText(ie.endpoint().host());

        return rowView;
    }
}
