package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET

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
    val instNo: Int?,
    val accountId: String?,             // The user's account ID
    val accountIdKey: String?,          // The unique account key
    val accountMode: AccountMode?,
    val accountDesc: String?,           // Description of account
    val accountName: String?,           // The nickname for the account
    val accountType: String?,           // The account type
    val institutionType: String?,       // BROKERAGE
    val accountStatus: AccountStatus?,
    val closedDate: Int?,               // The date when the account was closed
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountListResponse(
    @JsonProperty("AccountListResponse")
    val response: AccountRoot
)

interface AccountsApi {

    @GET("v1/accounts/list")
    fun getAccounts(): Call<AccountListResponse>

}