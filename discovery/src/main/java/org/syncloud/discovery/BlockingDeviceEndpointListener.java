package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.syncloud.ssh.model.DirectEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlockingDeviceEndpointListener implements DeviceEndpointListener {

    private static Logger logger = LogManager.getLogger(BlockingDeviceEndpointListener.class.getName());
    private List<DirectEndpoint> devices = new ArrayList<DirectEndpoint>();
    private final Phaser phaser = new Phaser(2);

    public List<DirectEndpoint> await(int timeout, TimeUnit timeUnit) {
        try {
            int services = phaser.getPhase();
            while (services >= 0) {
                logger.debug("will wait for service discovery, services: " + services);
                services  = phaser.awaitAdvanceInterruptibly(phaser.arrive(), timeout, timeUnit);
                logger.debug("done waiting: " + services);
            }

        } catch (InterruptedException e) {
            logger.error("interrupted", e);
        } catch (TimeoutException e) {
            logger.debug("no more waiting");
        }
        return devices;
    }

    @Override
    public void added(DirectEndpoint endpoint) {
        devices.add(endpoint);
        int services = phaser.arriveAndAwaitAdvance();
        logger.debug("discovered: " + services);
    }

    @Override
    public void removed(DirectEndpoint endpoint) {

    }
}
