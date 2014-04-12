package org.syncloud.discovery;

import com.google.common.base.Optional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import javax.jmdns.JmDNS;

public class Discovery {

    private static Logger logger = LogManager.getLogger(Discovery.class.getName());

    public static final String TYPE = "_http._tcp.local.";

    public static Optional<String> getUrl(int ipAddress, String serviceName) {


        byte[] ip = ByteBuffer.allocate(4).putInt(ipAddress).array();
        InetAddress myAddress;
        try {
            myAddress = InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            logger.debug("Failed to get address: " + e.toString());
            return Optional.absent();
        }

        try {
            EventListener listener = new EventListener(serviceName);
            logger.info("creating jmdns");
            JmDNS jmdns = JmDNS.create(myAddress);
            jmdns.addServiceListener(TYPE, listener);
            Optional<String> url = listener.getUrl();
            jmdns.removeServiceListener(TYPE, listener);
            logger.info("closing jmdns");
            jmdns.close();
            logger.info("closing jmdns: done");
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return Optional.absent();
        }
    }
}