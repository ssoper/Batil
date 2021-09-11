package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Alert(
    val id: Int,              // The numeric alert ID
    val createTime: Instant?, // The date and time the alert was issued, in Epoch time
    val subject: String?,     // The subject of the alert
    val status: Status?,      // UNREAD, READ, DELETED, UNDELETED
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertsResponse(
    val totalAlerts: Int,    // The total number of alerts for the user including READ, UNREAD and DELETED
    @JsonProperty("Alert")
    val alerts: List<Alert> // The array of alert responses
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertsResponseEnvelope(
    @JsonProperty("AlertsResponse")
    val response: AlertsResponse
)

interface AlertsApi {

    @GET("v1/user/alerts")
    fun getAlerts(@QueryMap options: Map<String, String>): Call<AlertsResponseEnvelope>

}