package org.syncloud.spm.model;

public class App {

    private String id;
    private String name;
    private String type;

    private Boolean installed;
    private String version;
    private String script;
    private String installedVersion;

    public App() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    public Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        this.installed = installed;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    public Type getAppType() {
        try {
            return Type.valueOf(type);
        } catch (Exception ignored) {
            return Type.unknown;
        }
    }

    public static enum Type {unknown, system, admin, user, dev}
}
