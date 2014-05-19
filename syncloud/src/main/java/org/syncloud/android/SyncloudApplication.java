package org.syncloud.android;

import android.app.Application;

import org.syncloud.android.adapter.DevicesAdapter;

public class SyncloudApplication extends Application {
    private DevicesAdapter devicesAdapter;

    public DevicesAdapter getDevicesAdapter() {
        return devicesAdapter;
    }

    public void setDevicesAdapter(DevicesAdapter devicesAdapter) {
        this.devicesAdapter = devicesAdapter;
    }
}
