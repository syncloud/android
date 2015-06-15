package org.syncloud.platform.ssh;

import org.junit.Test;
import org.syncloud.common.WebService;
import org.syncloud.platform.ssh.model.Identification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToolsTest {

    @Test
    public void testGetId() {
        WebService webService = mock(WebService.class);

        when(webService.execute(anyString(), anyString()))
                .thenReturn("{\"data\":{" +
                        "\"name\": \"test\"," +
                        "\"title\": \"test_title\"," +
                        "\"mac_address\": \"123321123\"" +
                        "}}, \"message\":\"good\", \"true\"");

        Tools tools = new Tools(webService);
        Identification id = tools.getId("host").get();

        assertEquals("test", id.name);
        assertEquals("test_title", id.title);
        assertEquals("123321123", id.mac_address);

    }
}
