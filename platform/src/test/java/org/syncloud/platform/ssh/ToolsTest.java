package org.syncloud.platform.ssh;

import org.junit.Test;
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
                .thenReturn("{\"data\":{" +
                        "\"name\": \"test\"," +
                        "\"title\": \"test_title\"," +
                        "\"mac_address\": \"123321123\"" +
                        "}}, \"message\":\"good\", \"true\"");

        Tools tools = new Tools(sshRunner);
        Identification id = tools.getId(null);

        assertEquals("test", id.name);
        assertEquals("test_title", id.title);
        assertEquals("123321123", id.mac_address);

    }
}
