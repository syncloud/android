package org.syncloud.android.ui.adapters;

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

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_item);
        this.activity = activity;
        preferences = ((SyncloudApplication) activity.getApplication()).getPreferences();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String mainDomain = preferences.getMainDomain();

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_item, null);

        TextView txtBoldTitle = (TextView) rowView.findViewById(R.id.txt_bold_title);
        TextView txtAdditionalLine = (TextView) rowView.findViewById(R.id.txt_additional_line);

        final DomainModel domain = getItem(position);

        String fullDomainName = domain.userDomain()+"."+mainDomain;
        txtBoldTitle.setText(fullDomainName);

        txtAdditionalLine.setText(domain.id().title());

        return rowView;
    }

}
