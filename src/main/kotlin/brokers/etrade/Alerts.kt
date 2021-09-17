package com.seansoper.batil.brokers.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant

enum class Category {
    STOCK,
    ACCOUNT
}

enum class Status {
    READ,
    UNREAD,
    DELETED,
    UNDELETED
}

class Alerts(
    session: Session,
    production: Boolean? = null,
    verbose: Boolean? = null,
    baseUrl: String? = null
) : Service(session, production, verbose, baseUrl) {

    /**
     * Retrieve a user’s alerts
     * @param[category] Alert category
     * @param[status] Alert status
     * @param[direction] Sort order of alerts
     * @param[search] Search is done based on the subject
     * @param[count] Max amount of alerts returned, default is 25, max is 300
     * @sample com.seansoper.batil.samples.Alerts.list
     */
    fun list(
        category: Category? = null,
        status: Status? = null,
        direction: TransactionSortOrder? = null,
        search: String? = null,
        count: Int? = null
    ): AlertsResponse? {

        val options: MutableMap<String, String> = mutableMapOf()

        category?.let {
            options.put("category", it.toString())
        }

        status?.let {
            options.put("status", it.toString())
        }

        direction?.let {
            options.put("direction", it.toString())
        }

        search?.let {
            options.put("search", it)
        }

        count?.let {
            options.put("count", it.toString())
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer(false))

        val service = createClient(AlertsApi::class.java, module)
        val response = service.getAlerts(options).execute()

        return response.body()?.response
    }

    /**
     * Retrieve details for a single a alert
     * @param[alertId] The id of the alert
     * @param[htmlTags] Indicates whether the returned text should have HTML tags included, default is false
     * @sample com.seansoper.batil.samples.Alerts.get
     */
    fun get(
        alertId: Int,
        htmlTags: Boolean? = null
    ): AlertDetails? {

        val options: MutableMap<String, String> = mutableMapOf()

        htmlTags?.let {
            options.put("htmlTags", it.toString())
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer(false))

        val service = createClient(AlertsApi::class.java, module)
        val response = service.getAlertDetails(alertId.toString(), options).execute()

        return response.body()?.response
    }

    /**
     * Delete single alert
     * @param[alertId] The id of the alert to delete
     * @sample com.seansoper.batil.samples.Alerts.deleteSingle
     */
    fun delete(alertId: Int): DeleteAlertsResponse? {
        return delete(listOf(alertId))
    }

    /**
     * Delete multiple alerts
     * @param[alertId] The ids of the alerts to delete
     * @sample com.seansoper.batil.samples.Alerts.deleteMultiple
     */
    fun delete(alertId: List<Int>): DeleteAlertsResponse? {

        val service = createClient(AlertsApi::class.java)
        return try {
            service.delete(alertId.joinToString(",")).execute().body()?.response
        } catch (e: ServiceUnavailableError) {
            // Strangely, the E*TRADE API returns a non-available service error on non-existent alert IDs
            null
        }
    }
}
