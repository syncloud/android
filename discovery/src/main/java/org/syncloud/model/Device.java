package org.syncloud.model;

public class Device {
    private String ip;
    private int port;
    private String ownCloudPath;

    public Device(String ip, int port, String ownCloudPath) {
        this.ip = ip;
        this.port = port;
        this.ownCloudPath = ownCloudPath;
    }

    public String getOwnCloudUrl() {
        return  "http://" + ip + ":" + port + ownCloudPath;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (port != device.port) return false;
        if (ip != null ? !ip.equals(device.ip) : device.ip != null) return false;
        if (ownCloudPath != null ? !ownCloudPath.equals(device.ownCloudPath) : device.ownCloudPath != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (ownCloudPath != null ? ownCloudPath.hashCode() : 0);
        return result;
    }
}
