package org.syncloud.android.model;

public class InstallStatus {
    private boolean installed = false;
    private String version = "";
    private String message = "";

    public InstallStatus(boolean installed, String version, String message) {
        this.installed = installed;
        this.version = version;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getVersion() {
        return version;
    }

}
