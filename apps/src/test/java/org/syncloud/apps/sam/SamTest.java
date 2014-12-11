package org.syncloud.apps.sam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.io.Resources;

import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.SshResult;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.syncloud.apps.sam.Sam.cmd;
import static org.syncloud.common.model.Result.value;

public class SamTest {

    private Release testRelease = new TestRelease();

    @Test
    public void testRunNoArgs() {

        Ssh ssh = mock(Ssh.class);
        when(ssh.execute(any(Device.class), anyString())).thenReturn(Optional.of(""));

        Device device = mock(Device.class);

        Sam sam = new Sam(ssh, testRelease);
        sam.run(device, Commands.update);

        verify(ssh).execute(device, "sam update");
    }

    @Test
    public void testRunSuccess() throws IOException {
        String json = "{\n" +
                "    \"message\": \"installed app\",\n" +
                "    \"data\": {},\n" +
                "    \"success\": true\n" +
                "    }";

        Ssh ssh = mock(Ssh.class);
        when(ssh.execute(any(Device.class), anyString())).thenReturn(Optional.of(json));

        Device device = mock(Device.class);

        Sam sam = new Sam(ssh, testRelease);
        Result<String> result = sam.run(device, Commands.update);

        assertFalse(result.hasError());
        assertEquals("installed app", result.getValue());

    }

    @Test
    public void testRunError() throws IOException {
        String json = "{\n" +
                "    \"message\": \"unable to install app\",\n" +
                "    \"data\": {},\n" +
                "    \"success\": false\n" +
                "    }";

        Ssh ssh = mock(Ssh.class);
        when(ssh.execute(any(Device.class), anyString())).thenReturn(Optional.of(json));

        Device device = mock(Device.class);

        Sam sam = new Sam(ssh, testRelease);
        Result<String> result = sam.run(device, Commands.update);

        assertTrue(result.hasError());
        assertEquals("unable to install app", result.getError());

    }

    @Test
    public void testRunWithArgs() {

        Ssh ssh = mock(Ssh.class);
        when(ssh.execute(any(Device.class), anyString())).thenReturn(Optional.of(""));

        Device device = mock(Device.class);

        Sam sam = new Sam(ssh, testRelease);
        sam.run(device, Commands.update, "--release", "0.1");

        verify(ssh).execute(device, "sam update --release 0.1");
    }

    @Test
    public void testList() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.json"), UTF_8);
        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        Optional<List<AppVersions>> result = sam.list(device);
        assertEquals(9, result.get().size());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testListEmpty() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.empty.json"), UTF_8);
        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        Optional<List<AppVersions>> result = sam.list(device);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().size());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testListEmptyReply() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = "";
        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        assertFalse(sam.list(device).isPresent());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testListCorrupted() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.error.json"), UTF_8);

        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        assertFalse(sam.list(device).isPresent());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testUpdateNoUpdates() throws IOException {
        assertUpdate("ok.no.updates.json", 0);
    }

    @Test
    public void testUpdateSomeUpdates() throws IOException {
        assertUpdate("ok.some.updates.json", 2);
    }

    private void assertUpdate(String response, int expectedUpdates) throws IOException {
        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource(response), UTF_8);
        String command = cmd(Commands.update, "--release", testRelease.getVersion());
        when(ssh.execute(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        assertEquals(sam.update(device).get().size(), expectedUpdates);

        verify(ssh).execute(device, command);
    }

    class TestRelease implements Release {
        @Override
        public String getVersion() {
            return "TEST";
        }
    }

}
