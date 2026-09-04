package org.syncloud.android

import android.util.Log

class Logger(tag: String) {

    private val tag = tag.take(MAX_TAG_LENGTH)

    fun debug(message: String?) {
        Log.d(tag, message ?: "")
    }

    fun info(message: String?) {
        Log.i(tag, message ?: "")
    }

    fun error(message: String?) {
        Log.e(tag, message ?: "")
    }

    fun error(message: String?, e: Throwable) {
        Log.e(tag, message ?: "", e)
    }

    companion object {
        private const val MAX_TAG_LENGTH = 23

        fun getLogger(clazz: Class<*>): Logger = Logger(clazz.simpleName)

        fun getLogger(name: String): Logger = Logger(name.substringAfterLast('.'))
    }
}
