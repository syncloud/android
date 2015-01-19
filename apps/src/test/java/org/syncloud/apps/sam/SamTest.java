package org.syncloud.apps.sam;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import org.junit.Test;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;

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

public class SamTest {

    private Release testRelease = new TestRelease();

    @Test
    public void testRunNoArgs() {

        SshRunner ssh = mock(SshRunner.class);
        when(ssh.run(any(ConnectionPointProvider.class), anyString())).thenReturn(Optional.of(""));

        ConnectionPointProvider device = mock(ConnectionPointProvider.class);

        Sam sam = new Sam(ssh, testRelease);
        sam.run(device, Commands.update);

        verify(ssh).run(device, "sam update");
    }

    @Test
    public void testRunSuccess() throws IOException {
        String json = "{\n" +
                "    \"message\": \"installed app\",\n" +
                "    \"data\": {},\n" +
                "    \"success\": true\n" +
                "    }";

        SshRunner ssh = mock(SshRunner.class);
        when(ssh.run(any(ConnectionPointProvider.class), anyString())).thenReturn(Optional.of(json));

        ConnectionPointProvider device = mock(ConnectionPointProvider.class);

        Sam sam = new Sam(ssh, testRelease);
        Boolean result = sam.run(device, Commands.update);

        assertTrue(result);

    }

    @Test
    public void testRunError() throws IOException {
        String json = "{\n" +
                "    \"message\": \"unable to install app\",\n" +
                "    \"data\": {},\n" +
                "    \"success\": false\n" +
                "    }";

        SshRunner ssh = mock(SshRunner.class);
        when(ssh.run(any(ConnectionPointProvider.class), anyString())).thenReturn(Optional.of(json));

        ConnectionPointProvider device = mock(ConnectionPointProvider.class);

        Sam sam = new Sam(ssh, testRelease);
        Boolean result = sam.run(device, Commands.update);

        assertFalse(result);

    }

    @Test
    public void testRunWithArgs() {

        SshRunner ssh = mock(SshRunner.class);
        when(ssh.run(any(ConnectionPointProvider.class), anyString())).thenReturn(Optional.of(""));

        ConnectionPointProvider device = mock(ConnectionPointProvider.class);

        Sam sam = new Sam(ssh, testRelease);
        sam.run(device, Commands.update, "--release", "0.1");

        verify(ssh).run(device, "sam update --release 0.1");
    }

    @Test
    public void testList() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource("app.list.json"), UTF_8);
        String command = cmd(Commands.list);
        when(ssh.run(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        Optional<List<AppVersions>> result = sam.list(device);
        assertEquals(9, result.get().size());

        verify(ssh).run(device, command);
    }

    @Test
    public void testListEmpty() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource("app.list.empty.json"), UTF_8);
        String command = cmd(Commands.list);
        when(ssh.run(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        Optional<List<AppVersions>> result = sam.list(device);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().size());

        verify(ssh).run(device, command);
    }

    @Test
    public void testListEmptyReply() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = "";
        String command = cmd(Commands.list);
        when(ssh.run(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        assertFalse(sam.list(device).isPresent());

        verify(ssh).run(device, command);
    }

    @Test
    public void testListCorrupted() throws IOException {

        SshRunner ssh = mock(SshRunner.class);
        ConnectionPointProvider device = mock(ConnectionPointProvider.class);
        String json = Resources.toString(getResource("app.list.error.json"), UTF_8);

        String command = cmd(Commands.list);
        when(ssh.run(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        assertFalse(sam.list(device).isPresent());

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
        String command = cmd(Commands.update, "--release", testRelease.getVersion());
        when(ssh.run(device, command)).thenReturn(Optional.of(json));

        Sam sam = new Sam(ssh, testRelease);

        assertEquals(sam.update(device).get().size(), expectedUpdates);

        verify(ssh).run(device, command);
    }

    class TestRelease implements Release {
        @Override
        public String getVersion() {
            return "TEST";
        }
    }

}
