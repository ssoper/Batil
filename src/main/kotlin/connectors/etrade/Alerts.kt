package com.seansoper.batil.connectors.etrade

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

    fun delete(alertId: Int): DeleteAlertsResponse? {
        return delete(listOf(alertId))
    }

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
