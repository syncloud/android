package org.syncloud.android.core.redirect

import org.syncloud.android.core.common.jackson.Jackson.createObjectMapper
import org.syncloud.android.core.redirect.UserStorage
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.log4j.Logger
import org.syncloud.android.core.redirect.model.User
import java.io.File
import java.io.IOException

class UserStorage(private val file: File) {
    fun load(): User? {
        return try {
            if (!file.exists()) {
                logger.error("User storage file does not exist: " + file.absolutePath)
                return null
            }
            mapper.readValue(file, User::class.java)
        } catch (ex: IOException) {
            logger.error("Failed to load user from cache", ex)
            null
        }
    }

    fun save(user: User?) {
        try {
            mapper.writeValue(file, user)
        } catch (ex: IOException) {
            logger.error("Failed to save user to cache", ex)
        }
    }

    companion object {
        private val logger = Logger.getLogger(
            UserStorage::class.java
        )
        private val mapper = createObjectMapper()
    }
}