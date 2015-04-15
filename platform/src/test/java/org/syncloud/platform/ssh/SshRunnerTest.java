package org.syncloud.platform.ssh;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.platform.ssh.SshRunner;

public class SshRunnerTest {
    @Test
    public void testCmdQuoted() {
        Assert.assertEquals("'password'", SshRunner.quotedCmd(new String[]{"password"}));
    }
}
