package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ui.DeviceAppsActivity;
import org.syncloud.platform.sam.AppVersions;

public class DeviceAppsAdapter extends ArrayAdapter<AppVersions> {
    private DeviceAppsActivity activity;

    public DeviceAppsAdapter(DeviceAppsActivity activity) {
        super(activity, R.layout.layout_app);
        this.activity = activity;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_app, null);

        TextView textView = (TextView) rowView.findViewById(R.id.app_name);
        ImageView appTypeUser = (ImageView) rowView.findViewById(R.id.app_icon_user);
        ImageView appTypeUtil = (ImageView) rowView.findViewById(R.id.app_icon_util);

        final AppVersions appVersions = getItem(position);
        textView.setText(appVersions.app.name + " " + appVersions.current_version);

        if (appVersions.app.ui) {
            appTypeUser.setVisibility(View.VISIBLE);
            appTypeUtil.setVisibility(View.GONE);
        } else {
            appTypeUtil.setVisibility(View.VISIBLE);
            appTypeUser.setVisibility(View.GONE);
        }

        return rowView;
    }
}
