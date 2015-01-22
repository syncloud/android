package org.syncloud.apps.server;

import com.google.common.base.Optional;

import org.junit.Test;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.Credentials;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerTest {
    @Test
    public void testActivate() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(ConnectionPointProvider.class), anyString())).thenReturn(Optional.of("" +
                "{\n" +
                "  \"message\": null, \n" +
                "  \"data\": {\n" +
                "    \"login\": \"root\", \n" +
                "    \"password\": \"syncloud\", \n" +
                "    \"key\": \"PRIVATE KEY\"\n" +
                "  }, \n" +
                "  \"success\": true\n" +
                "}"));
        Server server = new Server(runner);

        Optional<Credentials> credentials= server.activate(null, null, null, null, null, null, null);

        assertEquals("PRIVATE KEY", credentials.get().key());
    }


}
