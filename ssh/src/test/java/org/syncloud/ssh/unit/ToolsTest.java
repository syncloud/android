package org.syncloud.ssh.unit;

import com.google.common.base.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.Tools;
import org.syncloud.ssh.model.Credentials;
import org.syncloud.ssh.model.Endpoint;
import org.syncloud.ssh.model.Identification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToolsTest {

    @Test
    public void testGetId() {
        SshRunner sshRunner = mock(SshRunner.class);

        when(sshRunner.run(any(ConnectionPointProvider.class), anyString()))
                .thenReturn(Optional.of("{\"data\":{" +
                        "\"name\": \"test\"," +
                        "\"title\": \"test_title\"," +
                        "\"mac_address\": \"123321123\"" +
                        "}}, \"message\":\"good\", \"true\""));

        Tools tools = new Tools(sshRunner);
        Optional<Identification> id = tools.getId(null);

        assertTrue(id.isPresent());
        assertEquals("test", id.get().name);
        assertEquals("test_title", id.get().title);
        assertEquals("123321123", id.get().mac_address);

    }
}
