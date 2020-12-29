package com.seansoper.batil.connectors

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(val description: String,
                   val code: Int,
                   val type: String)

enum class QuoteStatus {
    REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED, UNKNOWN
}

val defaultDate = GregorianCalendar(1970, 1, 1, 1, 0, 0)

object DateSerializer {
    val Format = "HH:mm:ss zzz dd-MM-yyyy"
    val Formatter = SimpleDateFormat(Format)

    class Encode: JsonSerializer<GregorianCalendar>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: GregorianCalendar?, gen: JsonGenerator?, serializers: SerializerProvider?) {
            value?.apply {
                gen?.writeString(Formatter.format(time))
            }
        }
    }

    class Decode: JsonDeserializer<GregorianCalendar>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): GregorianCalendar {
            return p?.text?.let {
                val date = Formatter.parse(it)
                val calendar = GregorianCalendar()
                calendar.time = date

                calendar
            } ?: throw DeserializerException()
        }
    }

    class DeserializerException: JsonProcessingException("Could not parse JSON")
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteData(
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss zzz dd-MM-yyyy")
//    @JsonSerialize(using = DateSerializer.Encode::class)
//    @JsonDeserialize(using = DateSerializer.Decode::class)
    val dateTime: GregorianCalendar = defaultDate,

    val quoteStatus: QuoteStatus = QuoteStatus.UNKNOWN
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteResponse(
    @JsonProperty("QuoteData")
    val data: List<QuoteData> = listOf(QuoteData(defaultDate, QuoteStatus.UNKNOWN))
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TickerDataResponse(
    @JsonProperty("QuoteResponse")
    val response: QuoteResponse = QuoteResponse(
        data = listOf(QuoteData(defaultDate, QuoteStatus.UNKNOWN))
    )
)

interface Market {

    @GET("v1/market/quote/{symbol}")
    fun getQuote(@Path("symbol") symbol: String): Call<TickerDataResponse>

}