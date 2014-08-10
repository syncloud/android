package org.syncloud.ssh.model;

public class SshResult {
    private int exitCode = 0;
    private String message = "";

    public SshResult(int exitCode, String message) {
        this.exitCode = exitCode;
        this.message = message;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean ok() {
        return exitCode == 0;
    }
}
