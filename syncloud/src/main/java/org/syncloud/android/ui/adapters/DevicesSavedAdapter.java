package org.syncloud.android.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.DevicesSavedActivity;
import org.syncloud.android.core.platform.model.DomainModel;

public class DevicesSavedAdapter extends ArrayAdapter<DomainModel> {
    private final Preferences preferences;
    private DevicesSavedActivity activity;
    private String mainDomain;

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_item);
        this.activity = activity;
        preferences = ((SyncloudApplication) activity.getApplication()).getPreferences();
        mainDomain = preferences.getDomain();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_item, null);

        TextView txtBoldTitle = (TextView) rowView.findViewById(R.id.txt_bold_title);
        TextView txtAdditionalLine = (TextView) rowView.findViewById(R.id.txt_additional_line);

        final DomainModel domain = getItem(position);

        String fullDomainName = domain.userDomain()+"."+mainDomain;
        txtBoldTitle.setText(fullDomainName);

        txtBoldTitle.setTextColor(domain.hasDevice() ? Color.BLACK : Color.GRAY);

        txtAdditionalLine.setVisibility(domain.hasDevice() ? View.VISIBLE : View.INVISIBLE);


        if (domain.hasDevice()) {
            txtAdditionalLine.setText(domain.device().id().title());
        }

        return rowView;
    }

}
