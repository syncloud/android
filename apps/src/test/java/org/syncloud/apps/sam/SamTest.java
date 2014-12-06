package org.syncloud.apps.sam;

import com.google.common.io.Resources;

import org.junit.Test;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        Device device = mock(Device.class);

        Sam sam = new Sam(ssh, testRelease);
        sam.run(device, Commands.update);

        verify(ssh).execute(device, "sam update");
    }

    @Test
    public void testRunWithArgs() {

        Ssh ssh = mock(Ssh.class);
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
        when(ssh.execute(device, command)).thenReturn(value(json));

        Sam sam = new Sam(ssh, testRelease);

        Result<java.util.List<AppVersions>> result = sam.list(device);
        assertEquals(9, result.getValue().size());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testListEmpty() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.empty.json"), UTF_8);
        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(value(json));

        Sam sam = new Sam(ssh, testRelease);

        Result<java.util.List<AppVersions>> result = sam.list(device);
        assertFalse(result.hasError());
        assertEquals(0, result.getValue().size());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testListEmptyReply() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = "";
        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(value(json));

        Sam sam = new Sam(ssh, testRelease);

        assertTrue(sam.list(device).hasError());

        verify(ssh).execute(device, command);
    }

    @Test
    public void testListCorrupted() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.error.json"), UTF_8);

        String command = cmd(Commands.list);
        when(ssh.execute(device, command)).thenReturn(value(json));

        Sam sam = new Sam(ssh, testRelease);

        assertTrue(sam.list(device).hasError());

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
        when(ssh.execute(device, command)).thenReturn(value(json));

        Sam sam = new Sam(ssh, testRelease);

        assertEquals(sam.update(device).getValue().size(), expectedUpdates);

        verify(ssh).execute(device, command);
    }

    class TestRelease implements Release {
        @Override
        public String getVersion() {
            return "TEST";
        }
    }

}
