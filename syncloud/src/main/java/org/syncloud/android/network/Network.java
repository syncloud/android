package org.syncloud.android.network;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Network {

    private static Logger logger = Logger.getLogger(Network.class.getName());

    private WifiManager wifi;

    public Network(WifiManager wifi) {
        this.wifi = wifi;
    }

    public Optional<InetAddress> ip() {
        WifiInfo connInfo = wifi.getConnectionInfo();
        int ipAddress = connInfo.getIpAddress();
        byte[] ip = ByteBuffer.allocate(4).putInt(ipAddress).array();
        InetAddress myAddress;
        try {
            myAddress = InetAddress.getByAddress(ip);
            logger.debug("address: " + myAddress);
            return Optional.of(myAddress);
        } catch (UnknownHostException e) {
            logger.error("Failed to get address: " + e.toString());
            return Optional.absent();
        }
    }

    public Optional<String> hostname() {
        Optional<InetAddress> ip = ip();
        if(!ip.isPresent())
            return Optional.absent();
        List<String> split = Arrays.asList(ip.get().getHostAddress().split("\\."));
        Collections.reverse(split);
        return Optional.of(Joiner.on(".").join(split));
    }
}
