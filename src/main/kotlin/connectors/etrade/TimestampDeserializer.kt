package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.Instant

class TimestampDeserializer: JsonDeserializer<Instant>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Instant {
        return p?.longValue?.let {
            Instant.ofEpochSecond(it)
        } ?: throw TimestampDeserializerException()
    }

    class TimestampDeserializerException: JsonProcessingException("Could not parse timestamp from JSON")
}