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

enum class AccountType {
    AMMCHK, ARO, BCHK, BENFIRA, BENFROTHIRA, BENF_ESTATE_IRA, BENF_MINOR_IRA, BENF_ROTH_ESTATE_IRA, BENF_ROTH_MINOR_IRA, BENF_ROTH_TRUST_IRA, BENF_TRUST_IRA, BRKCD, BROKER, CASH, C_CORP, CONTRIBUTORY, COVERDELL_ESA, CONVERSION_ROTH_IRA, CREDITCARD, COMM_PROP, CONSERVATOR, CORPORATION, CSA, CUSTODIAL, DVP, ESTATE, EMPCHK, EMPMMCA, ETCHK, ETMMCHK, HEIL, HELOC, INDCHK, INDIVIDUAL, INDIVIDUAL_K, INVCLUB, INVCLUB_C_CORP, INVCLUB_LLC_C_CORP, INVCLUB_LLC_PARTNERSHIP, INVCLUB_LLC_S_CORP, INVCLUB_PARTNERSHIP, INVCLUB_S_CORP, INVCLUB_TRUST, IRA_ROLLOVER, JOINT, JTTEN, JTWROS, LLC_C_CORP, LLC_PARTNERSHIP, LLC_S_CORP, LLP, LLP_C_CORP, LLP_S_CORP, IRA, IRACD, MONEY_PURCHASE, MARGIN, MRCHK, MUTUAL_FUND, NONCUSTODIAL, NON_PROFIT, OTHER, PARTNER, PARTNERSHIP, PARTNERSHIP_C_CORP, PARTNERSHIP_S_CORP, PDT_ACCOUNT, PM_ACCOUNT, PREFCD, PREFIRACD, PROFIT_SHARING, PROPRIETARY, REGCD, ROTHIRA, ROTH_INDIVIDUAL_K, ROTH_IRA_MINORS, SARSEPIRA, S_CORP, SEPIRA, SIMPLE_IRA, TIC, TRD_IRA_MINORS, TRUST, VARCD, VARIRACD
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
    val accountType: AccountType?,      // The account type
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
            return status == AccountStatus.CLOSED
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