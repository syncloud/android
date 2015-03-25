package org.syncloud.platform.server;

import com.google.common.base.Optional;

import org.junit.Test;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.Credentials;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerTest {
    @Test
    public void testActivate() {
        SshRunner runner = mock(SshRunner.class);
        when(runner.run(any(ConnectionPointProvider.class), any(String[].class))).thenReturn(Optional.of("" +
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
