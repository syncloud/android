package org.syncloud.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.activity.DeviceAppsActivity;
import org.syncloud.model.App;

import static org.syncloud.ssh.Spm.Command.*;

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
        textView.setText(app.getName() + " " + app.getVersion());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openApp(app.getId());
            }
        });

        ImageButton install = (ImageButton) rowView.findViewById(R.id.install_app);
        install.setVisibility(View.GONE);
        ImageButton remove = (ImageButton) rowView.findViewById(R.id.remove_app);
        remove.setVisibility(View.GONE);
        ImageButton upgrade = (ImageButton) rowView.findViewById(R.id.upgrade_app);
        upgrade.setVisibility(View.GONE);

        if (app.getInstalled()) {
            if (!app.getInstalledVersion().equals(app.getVersion())) {
                upgrade.setVisibility(View.VISIBLE);
            }
            remove.setVisibility(View.VISIBLE);
        } else {
            install.setVisibility(View.VISIBLE);
        }

        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.run(Install, app.getId());
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.run(Remove, app.getId());
            }
        });
        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.run(Upgrade, app.getId());
            }
        });

        return rowView;

    }
}
