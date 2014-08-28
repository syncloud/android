package org.syncloud.ssh;

import org.syncloud.ssh.model.DirectEndpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EndpointVisibility {

    private int timeout = 3000;

    public boolean visible(DirectEndpoint endpoint) {
        try {
            Socket socket = new Socket();
            socket.setSoTimeout(timeout);
            socket.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()), timeout);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
