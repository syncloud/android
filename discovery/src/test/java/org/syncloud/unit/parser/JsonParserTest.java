package org.syncloud.unit.parser;

import com.google.common.io.ByteStreams;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.model.App;
import org.syncloud.model.Result;
import org.syncloud.model.SshResult;
import org.syncloud.parser.JsonParser;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.ByteStreams.toByteArray;

public class JsonParserTest {

    @Test
    public void testGoodSshResult() throws IOException {

        String response = new String(toByteArray(getClass().getResourceAsStream("/spm_list.json")));

        Result<List<App>> result = JsonParser.parse(new SshResult(0, response), App.class);
        Assert.assertFalse(result.hasError());

        List<App> apps = result.getValue();
        Assert.assertEquals(apps.size(), 2);

        Assert.assertEquals(apps.get(0).getId(), "id");
        Assert.assertEquals(apps.get(0).getName(), "test app");
        Assert.assertTrue(apps.get(0).getIsDev());
        Assert.assertTrue(apps.get(0).getInstalled());
        Assert.assertEquals(apps.get(0).getVersion(), "v0.2");
        Assert.assertEquals(apps.get(0).getScript(), "script");
        Assert.assertEquals(apps.get(0).getInstalledVersion(), "v0.1");

    }

    @Test
    public void testSshError() {

        Result<List<App>> result = JsonParser.parse(new SshResult(1, "error"), App.class);
        Assert.assertTrue(result.hasError());
        Assert.assertEquals(result.getError(), "error");

    }

    @Test
    public void testParseError() {

        Result<List<App>> result = JsonParser.parse(new SshResult(0, "error"), App.class);
        Assert.assertTrue(result.hasError());
        Assert.assertTrue(result.getError().contains("Unrecognized token"));

    }
}
