package org.syncloud.discovery.unit.parser;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.spm.model.App;
import org.syncloud.insider.model.InsiderPortMappingResult;
import org.syncloud.insider.model.PortMapping;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.model.SshResult;
import org.syncloud.ssh.parser.JsonParser;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.ByteStreams.toByteArray;
import static org.junit.Assert.assertEquals;

public class JsonParserTest {

    @Test
    public void testAppList() throws IOException {

        String response = new String(toByteArray(getClass().getResourceAsStream("/spm_list.json")));

        Result<List<App>> result = JsonParser.parse(new SshResult(0, response), App.class);
        Assert.assertFalse(result.hasError());

        List<App> apps = result.getValue();
        assertEquals(apps.size(), 2);

        assertEquals(apps.get(0).getId(), "id");
        assertEquals(apps.get(0).getName(), "test app");
        assertEquals(apps.get(0).getAppType(), App.Type.admin);
        Assert.assertTrue(apps.get(0).getInstalled());
        assertEquals(apps.get(0).getVersion(), "v0.2");
        assertEquals(apps.get(0).getScript(), "script");
        assertEquals(apps.get(0).getInstalledVersion(), "v0.1");

    }

    @Test
    public void testPortMapping() throws IOException {

        String response = new String(toByteArray(getClass().getResourceAsStream("/port_mapping.json")));

        Result<InsiderPortMappingResult> result = JsonParser.parseSingle(new SshResult(0, response), InsiderPortMappingResult.class);
        Assert.assertFalse(result.hasError());

        List<PortMapping> mappings = result.getValue().getData();
        assertEquals(mappings.size(), 2);

        assertEquals(mappings.get(0).getExternal_port(), 10000);
        assertEquals(mappings.get(0).getLocal_port(), 1020);
        assertEquals(mappings.get(1).getExternal_port(), 10001);
        assertEquals(mappings.get(1).getLocal_port(), 1021);

    }

    @Test
    public void testSshError() {

        Result<List<App>> result = JsonParser.parse(new SshResult(1, "error"), App.class);
        Assert.assertTrue(result.hasError());
        assertEquals(result.getError(), "error");

    }

    @Test
    public void testParseError() {

        Result<List<App>> result = JsonParser.parse(new SshResult(0, "error"), App.class);
        Assert.assertFalse(result.hasError());
        Assert.assertTrue(result.getValue().isEmpty());

    }
}
