package org.syncloud.apps.sam;

import com.google.common.io.Resources;

import org.junit.Test;
import org.syncloud.apps.FailOnErrorProgress;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.syncloud.apps.sam.Command.List;
import static org.syncloud.apps.sam.Command.Update;
import static org.syncloud.common.model.Result.value;

public class SamTest {

    @Test
    public void testRunNoArgs() {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        FailOnErrorProgress progress = new FailOnErrorProgress();

        Sam sam = new Sam(ssh);
        sam.run(device, progress, Command.Update);

        verify(ssh).execute(device, "sam update", progress);
    }

    @Test
    public void testRunWithArgs() {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);

        Sam sam = new Sam(ssh);
        FailOnErrorProgress progress = new FailOnErrorProgress();
        sam.run(device, progress, Command.Update, "--release 0.1");

        verify(ssh).execute(device, "sam update --release 0.1", progress);
    }

    @Test
    public void testList() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.json"), UTF_8);
        FailOnErrorProgress progress = new FailOnErrorProgress();
        when(ssh.execute(device, List.cmd(), progress)).thenReturn(value(json));

        Sam sam = new Sam(ssh);

        Result<java.util.List<AppVersions>> result = sam.list(device, progress);
        assertEquals(9, result.getValue().size());

        verify(ssh).execute(device, List.cmd(), progress);

    }

    @Test
    public void testListEmpty() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.empty.json"), UTF_8);
        FailOnErrorProgress progress = new FailOnErrorProgress();
        when(ssh.execute(device, List.cmd(), progress)).thenReturn(value(json));

        Sam sam = new Sam(ssh);

        Result<java.util.List<AppVersions>> result = sam.list(device, progress);
        assertFalse(result.hasError());
        assertEquals(0, result.getValue().size());

        verify(ssh).execute(device, List.cmd(), progress);
    }

    @Test
    public void testListEmptyReply() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = "";
        FailOnErrorProgress progress = new FailOnErrorProgress();

        when(ssh.execute(device, List.cmd(), progress)).thenReturn(value(json));

        Sam sam = new Sam(ssh);

        assertTrue(sam.list(device, progress).hasError());

        verify(ssh).execute(device, List.cmd(), progress);
    }

    @Test
    public void testListCorrupted() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.error.json"), UTF_8);
        FailOnErrorProgress progress = new FailOnErrorProgress();

        when(ssh.execute(device, List.cmd(), progress)).thenReturn(value(json));

        Sam sam = new Sam(ssh);

        assertTrue(sam.list(device, progress).hasError());

        verify(ssh).execute(device, List.cmd(), progress);
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
        String command = Update.cmd("--release", Sam.RELEASE);
        FailOnErrorProgress progress = new FailOnErrorProgress();
        when(ssh.execute(device, command, progress)).thenReturn(value(json));

        Sam sam = new Sam(ssh);

        assertEquals(sam.update(device, progress).getValue().size(), expectedUpdates);

        verify(ssh).execute(device, command, progress);
    }

}
