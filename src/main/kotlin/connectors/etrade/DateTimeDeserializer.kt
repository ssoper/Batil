package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

class DateTimeDeserializer : JsonDeserializer<GregorianCalendar>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): GregorianCalendar {
        return p?.text?.let {
            val date = SimpleDateFormat("HH:mm:ss zzz dd-MM-yyyy").parse(it)
            val calendar = GregorianCalendar()
            calendar.time = date

            calendar
        } ?: throw DateDeserializerException()
    }

    class DateDeserializerException : JsonProcessingException("Could not parse date from JSON")
}
