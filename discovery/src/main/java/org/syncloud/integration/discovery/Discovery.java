package org.syncloud.integration.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import javax.jmdns.JmDNS;

public class Discovery {

    private static Logger logger = LogManager.getLogger(Discovery.class.getName());

    public static final String TYPE = "_http._tcp.local.";
    private JmDNS jmdns;
    private EventToDeviceConverter listener;
    private boolean started = false;

    public Discovery(DeviceListener deviceListener, String serviceName) {
        listener = new EventToDeviceConverter(serviceName, deviceListener);
    }

    public void start(int ipAddress) {

        if (started){
            logger.error("already started, stop first");
            return;
        }


        byte[] ip = ByteBuffer.allocate(4).putInt(ipAddress).array();
        InetAddress myAddress;
        try {
            myAddress = InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            logger.debug("Failed to get address: " + e.toString());
            return;
        }

        try {
            logger.info("creating jmdns");
            jmdns = JmDNS.create(myAddress);
            jmdns.addServiceListener(TYPE, listener);
            started = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    public void stop() {

        if (!started) {
            logger.error("not started, start first");
            return;
        }

        try {
            jmdns.removeServiceListener(TYPE, listener);
            logger.info("closing jmdns");
            jmdns.close();
            logger.info("closing jmdns: done");
            started = false;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
}