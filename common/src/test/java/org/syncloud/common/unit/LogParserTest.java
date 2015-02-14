package org.syncloud.common.unit;

import com.google.common.base.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.syncloud.common.LogParser;
import org.syncloud.common.model.LogEvent;

public class LogParserTest {

    @Test
    public void testIncludeAppLogs() {

        LogParser parser = new LogParser();
        Optional<LogEvent> eventOpt = parser.parse("11-20 23:03:57.748 D/Class( 123): message1");

        Assert.assertTrue(eventOpt.isPresent());

        LogEvent event = eventOpt.get();
        Assert.assertEquals("11-20 23:03:57.748", event.getTimestamp());
        Assert.assertEquals("D", event.getLevel());
        Assert.assertEquals(123, event.getPid());
        Assert.assertEquals("Class", event.getTag());
        Assert.assertEquals("message1", event.getMessage());

    }

}
