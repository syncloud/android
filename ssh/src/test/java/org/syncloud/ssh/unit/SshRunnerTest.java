package org.syncloud.ssh.unit;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.ssh.SshRunner;

public class SshRunnerTest {
    @Test
    public void testShellEncoded() {
        Assert.assertEquals("\\$password", SshRunner.shellEncoded("$password"));
        Assert.assertEquals("\\$\\$password", SshRunner.shellEncoded("$$password"));
    }
}
