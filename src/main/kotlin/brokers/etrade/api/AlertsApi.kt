package com.seansoper.batil.brokers.etrade.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.seansoper.batil.brokers.etrade.services.AlertStatus
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.time.Instant

/**
 * @param[id] The numeric alert ID
 * @param[createTime] The date and time the alert was issued, in Epoch time
 * @param[subject] The subject of the alert
 * @param[status] UNREAD, READ, DELETED, UNDELETED
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Alert(
    val id: Int,
    val createTime: Instant?,
    val subject: String,
    val status: AlertStatus,
)

/**
 * @param[totalAlerts] The total number of alerts for the user including READ, UNREAD and DELETED
 * @param[alerts] The list of alert responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertsResponse(
    val totalAlerts: Int,
    @JsonProperty("Alert")
    val alerts: List<Alert>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertsResponseEnvelope(
    @JsonProperty("AlertsResponse")
    val response: AlertsResponse
)

/**
 * @param[id] The numeric alert ID
 * @param[createTime] The date and time the alert was issued, in Epoch time
 * @param[subject] The subject of the alert
 * @param[msgText] The text of the alert message
 * @param[readTime] The time the alert was read
 * @param[deleteTime] The time the alert was deleted
 * @param[symbol] The market symbol for the instrument related to this alert, if any; for example, GOOG. It is set only in case of Stock alerts.
 * @param[next] Contains url for next alert
 * @param[prev] Contains url for previous alert
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertDetails(
    val id: Int,
    val createTime: Instant,
    val subject: String,
    val msgText: String?,
    val readTime: Instant?,
    val deleteTime: Instant?,
    val symbol: String?,
    val next: String?,
    val prev: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlertDetailsResponse(
    @JsonProperty("AlertDetailsResponse")
    val response: AlertDetails
)

/**
 * @param[alertId] List of failed alerts
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FailedAlerts(
    val alertId: List<Int>,
)

/**
 * @param[result] Resulting code from deleting an alert, use the [succeeded] flag instead
 * @param[alerts] List of failed alerts, use [failedAlerts] to retrieve just the ids
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteAlertsResponse(
    val result: String,
    @JsonProperty("FailedAlerts")
    val alerts: FailedAlerts?,
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
    fun getAlertDetails(
        @Path("alertId") alertId: String,
        @QueryMap options: Map<String, String>
    ): Call<AlertDetailsResponse>

    @DELETE("/v1/user/alerts/{alertId}")
    fun delete(@Path("alertId") alertId: String): Call<DeleteAlertsEnvelope>
}
