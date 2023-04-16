package com.seansoper.batil.brokers.etrade.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class CustomBooleanDeserializer : JsonDeserializer<Boolean>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Boolean {
        return p?.text?.let {
            when (it) {
                "y" -> {
                    true
                }
                "n" -> {
                    false
                }
                else -> {
                    null
                }
            }
        } ?: throw CustomBooleanDeserializer()
    }

    class CustomBooleanDeserializer : JsonProcessingException("Could not parse boolean value from string")
}
