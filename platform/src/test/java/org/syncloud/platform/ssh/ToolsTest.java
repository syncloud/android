package org.syncloud.platform.ssh;

import com.google.common.base.Optional;

import org.junit.Test;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.Tools;
import org.syncloud.platform.ssh.model.Identification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToolsTest {

    @Test
    public void testGetId() {
        SshRunner sshRunner = mock(SshRunner.class);

        when(sshRunner.run(any(ConnectionPointProvider.class), any(String[].class)))
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
