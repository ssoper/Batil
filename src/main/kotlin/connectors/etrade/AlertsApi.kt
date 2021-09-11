package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Alert(
    val id: Int,              // The numeric alert ID
    val createTime: Instant?, // The date and time the alert was issued, in Epoch time
    val subject: String,      // The subject of the alert
    val status: Status,       // UNREAD, READ, DELETED, UNDELETED
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertDetails(
    val id: Int,              // The numeric alert ID
    val createTime: Instant,  // The date and time the alert was issued, in Epoch time
    val subject: String,      // The subject of the alert
    val msgText: String?,     // The text of the alert message
    val readTime: Instant?,   // The time the alert was read
    val deleteTime: Instant?, // The time the alert was deleted
    val symbol: String?,      // The market symbol for the instrument related to this alert, if any; for example, GOOG. It is set only in case of Stock alerts.
    val next: String?,        // Contains url for next alert
    val prev: String?,        // Contains url for previous alert
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertDetailsResponse(
    @JsonProperty("AlertDetailsResponse")
    val response: AlertDetails
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FailedAlerts(
    val alertId: List<Int>, // List of failed alerts
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteAlertsResponse(
    val result: String,             // SUCCESS, ERROR
    @JsonProperty("FailedAlerts")
    val alerts: FailedAlerts?,      // Object containing list of failed alerts
) {
    val succeeded: Boolean
        get() {
            return result == "SUCCESS"
        }
    val failedAlerts: List<Int>?
        get() {
            return alerts?.alertId
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteAlertsEnvelope(
    @JsonProperty("AlertsResponse")
    val response: DeleteAlertsResponse
)

interface AlertsApi {

    @GET("v1/user/alerts")
    fun getAlerts(@QueryMap options: Map<String, String>): Call<AlertsResponseEnvelope>

    @GET("/v1/user/alerts/{alertId}")
    fun getAlertDetails(@Path("alertId") alertId: String,
                        @QueryMap options: Map<String, String>): Call<AlertDetailsResponse>

    @DELETE("/v1/user/alerts/{alertId}")
    fun delete(@Path("alertId") alertId: String): Call<DeleteAlertsEnvelope>

}