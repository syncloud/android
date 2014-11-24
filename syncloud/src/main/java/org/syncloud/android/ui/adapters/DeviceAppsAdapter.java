package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ui.DeviceAppsActivity;
import org.syncloud.apps.sam.App;
import org.syncloud.apps.sam.AppVersions;

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
        final AppVersions appVersions = getItem(position);
        textView.setText(appVersions.app.name + " " + appVersions.current_version);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openApp(appVersions.app.id);
            }
        });

        ImageButton appTypeUser = (ImageButton) rowView.findViewById(R.id.app_icon_user);
        ImageButton appTypeUtil = (ImageButton) rowView.findViewById(R.id.app_icon_util);

        if (appVersions.app.appType() == App.Type.user) {
            appTypeUser.setVisibility(View.VISIBLE);
            appTypeUtil.setVisibility(View.GONE);
        } else {
            appTypeUtil.setVisibility(View.VISIBLE);
            appTypeUser.setVisibility(View.GONE);
        }

        return rowView;

    }
}
