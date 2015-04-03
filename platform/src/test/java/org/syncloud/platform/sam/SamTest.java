package org.syncloud.platform.sam;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import org.junit.Test;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.JsonApiException;
import org.syncloud.platform.ssh.model.SyncloudException;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.syncloud.platform.sam.Sam.cmd;

public class SamTest {

    private Release testRelease = new TestRelease();

    @Test
    public void testList() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource("app.list.json"), UTF_8);
        String[] command = cmd(new String[] {Commands.list});
        when(ssh.run(device, command)).thenReturn(json);

        Sam sam = new Sam(ssh, testRelease);

        List<AppVersions> result = sam.list(device);
        assertEquals(9, result.size());

        verify(ssh).run(device, command);
    }

    @Test
    public void testListEmpty() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource("app.list.empty.json"), UTF_8);
        String[] command = cmd(new String[] {Commands.list});
        when(ssh.run(device, command)).thenReturn(json);

        Sam sam = new Sam(ssh, testRelease);

        List<AppVersions> result = sam.list(device);
        assertEquals(0, result.size());

        verify(ssh).run(device, command);
    }

    @Test(expected=SyncloudException.class)
    public void testListEmptyReply() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = "";
        String[] command = cmd(new String[] {Commands.list});
        when(ssh.run(device, command)).thenReturn(json);

        Sam sam = new Sam(ssh, testRelease);

        sam.list(device);

        verify(ssh).run(device, command);
    }

    @Test(expected=SyncloudException.class)
    public void testListCorrupted() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource("app.list.error.json"), UTF_8);

        String[] command = cmd(new String[] {Commands.list});
        when(ssh.run(device, command)).thenReturn(json);

        Sam sam = new Sam(ssh, testRelease);

        sam.list(device);

        verify(ssh).run(device, command);
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
        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource(response), UTF_8);
        String[] command = cmd(new String[] {Commands.update, "--release", testRelease.getVersion()});
        when(ssh.run(device, command)).thenReturn(json);

        Sam sam = new Sam(ssh, testRelease);

        assertEquals(sam.update(device).size(), expectedUpdates);

        verify(ssh).run(device, command);
    }

    class TestRelease implements Release {
        @Override
        public String getVersion() {
            return "TEST";
        }
    }

}
