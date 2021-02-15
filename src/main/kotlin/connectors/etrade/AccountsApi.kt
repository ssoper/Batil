package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import java.time.Instant

enum class AccountMode {
    CASH, MARGIN
}

enum class AccountStatus {
    ACTIVE, CLOSED
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountRoot(
    @JsonProperty("Accounts")
    val accountRoot: AccountList
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountList(
    @JsonProperty("Account")
    val accounts: List<Account>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Account(
    val accountId: String?,             // The user's account ID
    val accountIdKey: String?,          // The unique account key
    val accountType: String?,           // The account type
    val institutionType: String?,       // BROKERAGE

    @JsonProperty("closedDate")
    val closedDateRaw: Int,           // The date when the account was closed

    @JsonProperty("accountMode")
    val mode: AccountMode?,

    @JsonProperty("accountDesc")  // Description of account
    val description: String?,

    @JsonProperty("accountName")  // The nickname for the account
    val name: String?,

    @JsonProperty("accountStatus")
    val status: AccountStatus?
) {
    val dateClosed: Instant?
        get() {
            return if (closedDateRaw > 0) {
                Instant.ofEpochSecond(closedDateRaw.toLong())
            } else {
                null
            }
        }

    val closed: Boolean
        get() {
            return closedDateRaw != 0
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountListResponse(
    @JsonProperty("AccountListResponse")
    val response: AccountRoot
)

interface AccountsApi {

    @GET("v1/accounts/list")
    fun getAccounts(): Call<AccountListResponse>

}