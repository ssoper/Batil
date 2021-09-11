package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

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

class Alerts(session: Session,
             production: Boolean? = null,
             verbose: Boolean? = null,
             baseUrl: String? = null): Service(session, production, verbose, baseUrl) {

    fun list(category: Category? = null,
             status: Status? = null,
             direction: TransactionSortOrder? = null,
             search: String? = null,
             count: Int? = null): AlertsResponse? {

        val options: MutableMap<String, String> = mutableMapOf()

        category?.let {
            options.putAll(mapOf(
                "category" to it.toString()
            ))
        }

        status?.let {
            options.putAll(mapOf(
                "status" to it.toString()
            ))
        }

        direction?.let {
            options.putAll(mapOf(
                "direction" to it.toString()
            ))
        }

        search?.let {
            options.putAll(mapOf(
                "search" to it
            ))
        }

        count?.let {
            options.putAll(mapOf(
                "count" to it.toString()
            ))
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer(false))

        val service = createClient(AlertsApi::class.java, module)
        val response = service.getAlerts(options).execute()

        return response.body()?.response
    }

    fun get(alertId: Int,
            htmlTags: Boolean? = null): AlertDetails? {

        val options: MutableMap<String, String> = mutableMapOf()

        htmlTags?.let {
            options.putAll(mapOf(
                "htmlTags" to it.toString()
            ))
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer(false))

        val service = createClient(AlertsApi::class.java, module)
        val response = service.getAlertDetails(alertId.toString(), options).execute()

        return response.body()?.response
    }
}
