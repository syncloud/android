package org.syncloud.android.discovery;

import java.util.Date;

public class Event {
    public Date time = new Date();
    public String type;

    public Event(String type) {
        this.type = type;
    }
}
