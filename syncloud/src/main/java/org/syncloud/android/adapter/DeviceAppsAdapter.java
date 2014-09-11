package org.syncloud.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.activity.DeviceAppsActivity;
import org.syncloud.apps.spm.App;

import static org.syncloud.apps.spm.Spm.Command.*;

public class DeviceAppsAdapter extends ArrayAdapter<App> {
    private DeviceAppsActivity activity;

    public DeviceAppsAdapter(DeviceAppsActivity activity) {
        super(activity, R.layout.layout_app);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_app, null);
        TextView textView = (TextView) rowView.findViewById(R.id.app_name);
        final App app = getItem(position);
        textView.setText(app.name + " " + app.version);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openApp(app.id);
            }
        });

        ImageButton install = (ImageButton) rowView.findViewById(R.id.install_app);
        install.setVisibility(View.GONE);
        ImageButton remove = (ImageButton) rowView.findViewById(R.id.remove_app);
        remove.setVisibility(View.GONE);
        ImageButton upgrade = (ImageButton) rowView.findViewById(R.id.upgrade_app);
        upgrade.setVisibility(View.GONE);

        ImageButton appTypeUser = (ImageButton) rowView.findViewById(R.id.app_icon_user);
        ImageButton appTypeUtil = (ImageButton) rowView.findViewById(R.id.app_icon_util);

        if (app.appType() == App.Type.user) {
            appTypeUser.setVisibility(View.VISIBLE);
            appTypeUtil.setVisibility(View.GONE);
        } else {
            appTypeUtil.setVisibility(View.VISIBLE);
            appTypeUser.setVisibility(View.GONE);
        }

        if (app.installed()) {
            if (!app.installed_version.equals(app.version)) {
                upgrade.setVisibility(View.VISIBLE);
            }
            remove.setVisibility(View.VISIBLE);
        } else {
            install.setVisibility(View.VISIBLE);
        }

        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.run(Install, app.id);
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.run(Remove, app.id);
            }
        });
        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.run(Upgrade, app.id);
            }
        });

        return rowView;

    }
}
