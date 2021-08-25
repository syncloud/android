package org.syncloud.android

interface Progress {
    fun start()
    fun stop()
    fun error(message: String?)
    fun title(title: String?)
    open class Empty : Progress {
        override fun start() {}
        override fun stop() {}
        override fun error(message: String?) {}
        override fun title(title: String?) {}
    }
}