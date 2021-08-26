package org.syncloud.android.core.common.jackson

import com.fasterxml.jackson.databind.JsonDeserializer
import kotlin.Throws
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RFC822DateTimeDeserializer : JsonDeserializer<Date?>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        jsonparser: JsonParser,
        deserializationcontext: DeserializationContext
    ): Date? {
        val format: DateFormat = SimpleDateFormat()
        val dateStr = jsonparser.text
        var date: Date? = null
        date = try {
            format.parse(dateStr)
        } catch (e: ParseException) {
            throw IOException("Can't parse DateTime from RFC-822 format", e)
        }
        return date
    }
}