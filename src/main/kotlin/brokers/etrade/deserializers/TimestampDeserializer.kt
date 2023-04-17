package com.seansoper.batil.brokers.etrade.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class TimestampDeserializer() : JsonDeserializer<Instant>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Instant {
        return p?.longValue?.let {
            val instant = Instant.ofEpochMilli(it)
            val zoned = ZonedDateTime.ofInstant(instant, ZoneId.of("America/New_York"))

            if (zoned.year < 1971) {
                Instant.ofEpochSecond(it)
            } else {
                instant
            }
        } ?: throw TimestampDeserializerException()
    }

    class TimestampDeserializerException : JsonProcessingException("Could not parse timestamp from JSON")
}
