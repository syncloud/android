package org.syncloud.ssh.model;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

public class Device implements Serializable {
    private Integer id;
    private DeviceEndpoint externalEndpoint;
    private DeviceEndpoint localEndpoint;
    private String name;

    public Device(DeviceEndpoint externalEndpoint, DeviceEndpoint localEndpoint) {
        this.externalEndpoint = externalEndpoint;
        this.localEndpoint = localEndpoint;
    }

    public Device(Integer id, String name, DeviceEndpoint externalEndpoint, DeviceEndpoint localEndpoint) {
        this(externalEndpoint, localEndpoint);
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getDisplayName() {
        Iterable<String> endpoints = transform(endpoints(), new Function<DeviceEndpoint, String>() {
            @Override
            public String apply(DeviceEndpoint input) {
                return input.getHost();
            }
        });
        return name != null ? name : join(endpoints, "/");
    }

    public Iterable<DeviceEndpoint> endpoints() {
        List<DeviceEndpoint> hosts = asList(
                getExternalEndpoint(),
                getLocalEndpoint());
        return filter(hosts, notNull());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceEndpoint getExternalEndpoint() {
        return externalEndpoint;
    }

    public DeviceEndpoint getLocalEndpoint() {
        return localEndpoint;
    }

    @Override
    public String toString() {
        return "Device{" +
                "localEndpoint=" + localEndpoint +
                ", externalEndpoint=" + externalEndpoint +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (id != null ? !id.equals(device.id) : device.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
