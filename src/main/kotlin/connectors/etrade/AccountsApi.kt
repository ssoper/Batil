package com.seansoper.batil.connectors.etrade

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
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

enum class InstitutionType {
    BROKERAGE, GLOBALTRADING, NONUS, STOCKPLAN, LENDING, HELOC, HEIL, ONTRACK, GENPACT, AUTO, AUTOLOAN, BETA, LOYALTY, SBASKET, CC_BALANCETRANSFER, GENPACT_LEAD, GANIS, MORTGAGE, EXTERNAL, FUTURES, VISA, RJO, WDBH
}

enum class OptionLevel {
    NO_OPTIONS, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4
}

enum class QuoteMode {
    QUOTE_REALTIME, QUOTE_DELAYED, QUOTE_CLOSING, QUOTE_AHT_REALTIME, QUOTE_AHT_BEFORE_OPEN, QUOTE_AHT_CLOSING, QUOTE_NONE
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenCalls(
    val minEquityCall: Float?, // The minimum equity call
    val fedCall: Float?,       // The federal call
    val cashCall: Float?,      // The cash call
    val houseCall: Float?      // The house call
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RealTimeValues(
    val totalAccountValue: Float?, // The total account value
    val netMv: Float?,             // The net market value
    val netMvLong: Float?,         // The long net market value
    val netMvShort: Float?,        // The short net market value
    val totalLongValue: Float?,    // The total long value
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ComputedBalance(
    val cashAvailableForInvestment: Float?,     // The cash available for investments
    val cashAvailableForWithdrawal: Float?,     // The cash available for withdrawal
    val totalAvailableForWithdrawal: Float?,    // The total amount available for withdrawal
    val netCash: Float?,                        // The net cash balance
    val cashBalance: Float?,                    // The current cash balance
    val settledCashForInvestment: Float?,       // The settled cash for investments
    val unSettledCashForInvestment: Float?,     // The unsettled cash for investments
    val fundsWithheldFromPurchasePower: Float?, // The funds withheld from the purchasing power
    val fundsWithheldFromWithdrawal: Float?,    // The funds withheld from withdrawal
    val marginBuyingPower: Float?,              // The margin account buying power
    val cashBuyingPower: Float?,                // The cash account buying power
    val dtMarginBuyingPower: Float?,            // The day trader margin account buying power
    val dtCashBuyingPower: Float?,              // The day trader cash account buying power
    val marginBalance: Float?,                  // The margin account balance
    val shortAdjustBalance: Float?,             // The short adjusted balance
    val regtEquity: Float?,                     // The Regulation T equity
    val regtEquityPercent: Float?,              // The Regulation T equity percentage
    val accountBalance: Float?,                 // The current account balance

    @JsonProperty("OpenCalls")
    val openCalls: OpenCalls,

    @JsonProperty("RealTimeValues")
    val realTimeValues: RealTimeValues,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountCash(
    val fundsForOpenOrdersCash: Float?, // The funds reserved for open orders
    val moneyMktBalance: Float?,        // The current cash balance of the money market or sweep deposit account
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountBalance(
    val accountId: String?,          // The account ID for which the balance is requested
    val asOfDate: Int?,              // The effective date in UTC
    val quoteMode: Int,              // The quote type indicator: 0 = QUOTE REALTIME, 1 = QUOTE DELAYED, 2 = QUOTE CLOSING, 3 = QUOTE AHT REALTIME, 4 = QUOTE AHT BEFORE OPEN, 5 = QUOTE AHT CLOSING, 6 = QUOTE NONE
    val dayTraderStatus: String?,    // The user's status as a day trader
    val accountMode: AccountMode?,   // The account mode indicating the account's special privileges as a cash account, a margin account, and so on
    val accountType: AccountType?,
    val optionLevel: OptionLevel,
    val institutionType: InstitutionType?,

    @JsonProperty("accountDescription")
    val description: String?,

    @JsonProperty("Cash")
    val cash: AccountCash,

    @JsonProperty("Computed")
    val balances: ComputedBalance
) {
    val optionLevelValue: Int
        get() {
            return optionLevel.ordinal
        }

    val quoteModeValue: QuoteMode
        get() {
            return QuoteMode.values()[quoteMode]
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BalanceResponse(
    @JsonProperty("BalanceResponse")
    val response: AccountBalance
)

interface AccountsApi {

    @GET("v1/accounts/list")
    fun getAccounts(): Call<AccountListResponse>

    @GET("/v1/accounts/{accountIdKey}/balance?instType=BROKERAGE&realTimeNAV=true")
    fun getBalance(@Path("accountIdKey") accountIdKey: String): Call<BalanceResponse>

}