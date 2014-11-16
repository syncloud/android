package org.syncloud.ssh;

import com.jcraft.jsch.JSch;

public class JSchFactory {
    public JSch create() {
        return new JSch();
    }
}
