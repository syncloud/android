package org.syncloud.android.log;

import java.util.Date;

public class LogEvent {
    private Date date;
    private String level;
    private String message;

    public LogEvent(Date date, String level, String message) {
        this.date = date;
        this.level = level;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}
