package org.syncloud.android.core.common.jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import org.syncloud.android.core.common.jackson.RFC822DateTimeDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.DeserializationFeature
import java.util.*

object Jackson {
    @JvmStatic
    fun createObjectMapper(): ObjectMapper {
        val deserializer = RFC822DateTimeDeserializer()
        val module = SimpleModule("RFC822DateTimeDeserializerModule", Version(1, 0, 0, null, null, null))
        module.addDeserializer(Date::class.java, deserializer)
        val mapper = ObjectMapper()
        mapper.registerModule(module)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}