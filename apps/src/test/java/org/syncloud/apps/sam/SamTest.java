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
import static org.junit.Assert.fail;
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
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);
        sam.run(device, Command.Update);

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, "sam update");
    }

    @Test
    public void testRunWithArgs() {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);
        sam.run(device, Command.Update, "--release 0.1");

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, "sam update --release 0.1");
    }

    @Test
    public void testList() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.json"), UTF_8);
        when(ssh.execute(device, List.cmd())).thenReturn(value(json));
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);

        Result<java.util.List<AppVersions>> result = sam.list(device);
        assertFalse(result.hasError());
        assertEquals(8, result.getValue().size());

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, List.cmd());

    }

    @Test
    public void testListEmpty() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.empty.json"), UTF_8);
        when(ssh.execute(device, List.cmd())).thenReturn(value(json));
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);

        Result<java.util.List<AppVersions>> result = sam.list(device);
        if (result.hasError())
            fail(result.getError());
        assertFalse(result.hasError());
        assertEquals(0, result.getValue().size());

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, List.cmd());
    }

    @Test
    public void testListEmptyReply() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        when(ssh.execute(device, List.cmd())).thenReturn(value(""));
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);

        assertTrue(sam.list(device).hasError());

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, List.cmd());
    }

    @Test
    public void testListCorrupted() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("app.list.error.json"), UTF_8);
        when(ssh.execute(device, List.cmd())).thenReturn(value(json));
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);

        assertTrue(sam.list(device).hasError());

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, List.cmd());
    }

    @Test
    public void testUpdate() throws IOException {

        Ssh ssh = mock(Ssh.class);
        Device device = mock(Device.class);
        String json = Resources.toString(getResource("ok.json"), UTF_8);
        String command = Update.cmd("--release", Sam.RELEASE);
        when(ssh.execute(device, command)).thenReturn(value(json));
        when(ssh.execute(device, Sam.SAM_EXIST_COMMAND)).thenReturn(value("exist"));

        Sam sam = new Sam(ssh);

        Result<String> update = sam.update(device);
        assertFalse(update.hasError());

        verify(ssh).execute(device, Sam.SAM_EXIST_COMMAND);
        verify(ssh).execute(device, command);
    }
}
