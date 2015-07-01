package org.syncloud.android;

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.mindpipe.android.logging.log4j.LogCatAppender;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class ConfigureLog4J {
    public static void configure() {


        final Logger root = Logger.getRootLogger();
        final Layout messageLayout = new EnhancedPatternLayout("%m%n");
        final Layout tagLayout = new EnhancedPatternLayout("%c{1}");
        final LogCatAppender logCatAppender = new LogCatAppender(messageLayout, tagLayout);

        root.addAppender(logCatAppender);

//        final LogConfigurator logConfigurator = new LogConfigurator();
//        logConfigurator.setUseFileAppender(false);
//        logConfigurator.setRootLevel(Level.DEBUG);
//        Set log level of a specific logger
//        logConfigurator.setLevel("org.apache", Level.ERROR);
//        logConfigurator.configure();
    }
}