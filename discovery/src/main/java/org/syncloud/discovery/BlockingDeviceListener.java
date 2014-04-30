package org.syncloud.discovery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlockingDeviceListener implements DeviceLisener {

    private static Logger logger = LogManager.getLogger(BlockingDeviceListener.class.getName());
    private List<String> urls = new ArrayList<String>();
    private final Phaser phaser = new Phaser(2);

    public List<String> waitForServices(int timeout, TimeUnit timeUnit) {
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
        return urls;
    }

    @Override
    public void added(String url) {
        urls.add(url);
        int services = phaser.arriveAndAwaitAdvance();
        logger.debug("discovered: " + services);
    }

    @Override
    public void removed(String url) {

    }
}
