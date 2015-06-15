package org.syncloud.android.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.DevicesSavedActivity;
import org.syncloud.platform.ssh.model.DomainModel;

public class DevicesSavedAdapter extends ArrayAdapter<DomainModel> {
    private final Preferences preferences;
    private DevicesSavedActivity activity;
    private String mainDomain;

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_saved);
        this.activity = activity;
        preferences = ((SyncloudApplication) activity.getApplication()).getPreferences();
        mainDomain = preferences.getDomain();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_saved, null);

        TextView txtBoldTitle = (TextView) rowView.findViewById(R.id.txt_bold_title);
        TextView txtFirstLine = (TextView) rowView.findViewById(R.id.txt_first_line);
        TextView txtSecondLine = (TextView) rowView.findViewById(R.id.txt_second_line);
        ImageView imgKey = (ImageView) rowView.findViewById(R.id.img_key);

        final DomainModel domain = getItem(position);

        String fullDomainName = domain.userDomain()+"."+mainDomain;
        txtBoldTitle.setText(fullDomainName);

        txtBoldTitle.setTextColor(domain.hasDevice() ? Color.BLACK : Color.GRAY);

        txtFirstLine.setVisibility(domain.hasDevice() ? View.VISIBLE : View.INVISIBLE);
        txtSecondLine.setVisibility(domain.hasDevice() ? View.VISIBLE : View.INVISIBLE);
        imgKey.setVisibility(domain.hasDevice() ? View.VISIBLE : View.INVISIBLE);


        if (domain.hasDevice()) {
            txtFirstLine.setText(domain.device().id().title());
            txtSecondLine.setText(domain.device().id().macAddress());
        }

        if (!preferences.isDebug())
            txtSecondLine.setVisibility(View.GONE);

//        if (domain.hasKey())
//            imgKey.setImageResource(R.drawable.ic_action_accounts);
//        else
//            imgKey.setImageResource(R.drawable.ic_action_secure);

        return rowView;
    }

}
