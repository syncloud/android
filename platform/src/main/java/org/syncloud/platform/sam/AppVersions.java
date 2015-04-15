package org.syncloud.platform.sam;

public class AppVersions {
    public App app;
    public String current_version;
    public String installed_version;

    public Boolean installed() {
        return installed_version != null;
    }

    @Override
    public String toString() {
        return app.name;
    }
}
