package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.jmdns.JmDNS;

public class Discovery {

    private static Logger logger = LogManager.getLogger(Discovery.class.getName());

    public static final String TYPE = "_http._tcp.local.";

    public static List<String> getUrl(int ipAddress, String serviceName) {


        byte[] ip = ByteBuffer.allocate(4).putInt(ipAddress).array();
        InetAddress myAddress;
        try {
            myAddress = InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            logger.debug("Failed to get address: " + e.toString());
            return new ArrayList<String>();
        }

        try {
            EventListener listener = new EventListener(serviceName);
            logger.info("creating jmdns");
            JmDNS jmdns = JmDNS.create(myAddress);
            jmdns.addServiceListener(TYPE, listener);
            List<String> urls = listener.getUrl();
            jmdns.removeServiceListener(TYPE, listener);
            logger.info("closing jmdns");
            jmdns.close();
            logger.info("closing jmdns: done");
            return urls;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return new ArrayList<String>();
        }
    }
}