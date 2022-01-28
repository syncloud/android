package org.syncloud.android

import org.apache.log4j.EnhancedPatternLayout
import de.mindpipe.android.logging.log4j.LogCatAppender
import org.apache.log4j.Layout
import org.apache.log4j.Logger

object ConfigureLog4J {
    @JvmStatic
    fun configure() {
        val root = Logger.getRootLogger()
        val messageLayout: Layout = EnhancedPatternLayout("%m%n")
        val tagLayout: Layout = EnhancedPatternLayout("%c{1}")
        val logCatAppender = LogCatAppender(messageLayout, tagLayout)
        root.addAppender(logCatAppender)
    }
}