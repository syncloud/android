package org.syncloud.android.discovery;

import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class EventCache {

    public List<Event> events = new ArrayList<Event>();

    @Subscribe
    public void add(Event event) {
        events.add(event);
    }
}
