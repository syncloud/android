package org.syncloud.ssh.unit;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.ssh.SshRunner;

public class SshRunnerTest {
    @Test
    public void testCmdQuoted() {
        Assert.assertEquals("'password'", SshRunner.quotedCmd(new String[]{"password"}));
    }
}
