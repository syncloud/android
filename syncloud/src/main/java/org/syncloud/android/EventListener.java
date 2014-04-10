package org.syncloud.android;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

class EventListener implements ServiceListener {


    private Updater updater;

    public EventListener(Updater updater) {

        this.updater = updater;
    }

    @Override
    public void serviceAdded(final ServiceEvent event) {
        updater.update("add: " + event.getType());
        if (event.getName().equals("ownCloud")) {
            updater.update("add: " + event.getInfo().getPort());
            updater.update("add: " + event.getInfo().getServer());

            ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());

            updater.update("more: " + info.getType());
//                        Log.d("s", "more: " + info.getInet4Addresses()[0].getHostAddress());

            String server = info.getServer();
            String local = ".local.";
            if (server.endsWith(local))
                server = server.substring(0, server.length() - local.length());

            final String text = "URL: http://" + server + ":" + info.getPort() + info.getNiceTextString();

            updater.update("text: " + text);

            updater.update(text);

        }

    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        updater.update("removed: " + event.getType() + "" + event.getName());

    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        updater.update("resolved: " + event.getType());
        if (event.getName().equals("ownCloud")) {
            updater.update("resolved: " + event.getInfo().getPort());
            updater.update("resolved: " + event.getInfo().getServer());
        }
    }

}
