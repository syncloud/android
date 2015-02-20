package org.syncloud.common.log;

public class LogEvent {
    private String timestamp;
    private String level;
    private String tag;
    private int pid;
    private String message;

    public LogEvent(String timestamp, String level, String tag, int pid, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.pid = pid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public int getPid() {
        return pid;
    }

    public String getTag() {
        return tag;
    }
}
