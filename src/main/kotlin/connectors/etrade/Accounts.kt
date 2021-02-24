package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.databind.module.SimpleModule
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

enum class TransactionSortOrder {
    ASC, DESC
}

class Accounts(session: Session,
               production: Boolean? = null,
               verbose: Boolean? = null,
               baseUrl: String? = null): Service(session, production, verbose, baseUrl) {

    fun list(): List<Account>? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getAccounts().execute()

        return response.body()?.response?.accountRoot?.accounts
    }

    fun getBalance(accountIdKey: String): AccountBalance? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getBalance(accountIdKey).execute()

        return response.body()?.response
    }

    fun listTransactions(accountIdKey: String): TransactionResponse? {
        return listTransactions(accountIdKey, startDate = null, endDate = null, sortOrder = null, startAt = null)
    }

    fun listTransactions(accountIdKey: String,
                         startDate: GregorianCalendar,
                         endDate: GregorianCalendar): TransactionResponse? {
        return listTransactions(accountIdKey, startDate = startDate, endDate = endDate, sortOrder = null, startAt = null)
    }

    // x TODO: Fully consume transaction model
    // x TODO: List most recent 50 transactions
    // x TODO: Implement date querying
    // TODO: List first 50 transactions
    // TODO: Implement marker
    // TODO: Implement varying count
    // TODO: Implement sort

    fun formatDate(date: GregorianCalendar): String {
        val formatter = SimpleDateFormat("MMddyyyy")
        formatter.calendar = date
        return formatter.format(date.time)
    }

    fun listTransactions(accountIdKey: String,
                         startDate: GregorianCalendar?,
                         endDate: GregorianCalendar?,
                         sortOrder: TransactionSortOrder?,
                         startAt: TransactionId?,
                         count: Int? = 50): TransactionResponse? {

        val options = mutableMapOf("count" to count.toString())

        startDate?.let {
            options.putAll(mapOf(
                "startDate" to formatDate(startDate)
            ))
        }

        endDate?.let {
            options.putAll(mapOf(
                "endDate" to formatDate(endDate)
            ))
        }

        val module = SimpleModule()
        module.addDeserializer(Instant::class.java, TimestampDeserializer())

        val service = createClient(AccountsApi::class.java, module)
        val response = service.listTransactions(accountIdKey, options).execute()

        return response.body()?.response
    }
}
