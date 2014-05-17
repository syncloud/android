package org.syncloud.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.syncloud.android.activity.Device;
import org.syncloud.model.App;

import static org.syncloud.integration.ssh.Spm.Commnand.*;

public class AppsAdapter extends ArrayAdapter<App> {
    private Device activity;

    public AppsAdapter(Device activity) {
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

        Button install = (Button) rowView.findViewById(R.id.install_app);
        install.setVisibility(View.GONE);
        Button remove = (Button) rowView.findViewById(R.id.remove_app);
        remove.setVisibility(View.GONE);
        Button upgrade = (Button) rowView.findViewById(R.id.upgrade_app);
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
