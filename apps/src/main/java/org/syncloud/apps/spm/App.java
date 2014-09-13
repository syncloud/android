package org.syncloud.apps.spm;

public class App {

    public String id;
    public String name;
    public String type;

    public Type appType() {
        try {
            return Type.valueOf(type);
        } catch (Exception ignored) {
            return Type.unknown;
        }
    }

    public static enum Type {unknown, system, admin, user, dev}
}
