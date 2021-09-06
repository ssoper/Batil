import TestHelper.MockHelper.createServer
import TestHelper.MockHelper.mockSession
import TestHelper.PathHelper.randomString
import com.seansoper.batil.connectors.etrade.*
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths
import java.time.Instant
import java.util.*
import kotlin.random.Random.Default.nextLong

class AccountsTest: StringSpec({

    "list accounts" {
        val path = Paths.get("apiResponses/accounts/list.json")

        createServer(path) {
            val service = Accounts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.list()

            data.shouldNotBeNull()
            data.count().shouldBe(2)
            data[0].accountId.shouldBe("99991111")
            data[0].dateClosed.shouldBeNull()
            data[0].closed.shouldBe(false)
            data[0].accountType.shouldBe(AccountType.INDIVIDUAL)

            data[1].accountId.shouldBe("11112222")
            data[1].dateClosed.shouldNotBeNull()
            data[1].closed.shouldBe(true)
            val dateClosed = Instant.ofEpochSecond(1400756700L) // 2014-05-22T11:05:00Z
            data[1].dateClosed.shouldBe(dateClosed)
            data[1].accountType.shouldBe(AccountType.IRA_ROLLOVER)

            it.takeRequest().path.shouldBe("/v1/accounts/list")
        }
    }

    "get balance" {
        val path = Paths.get("apiResponses/accounts/balance.json")

        createServer(path) {
            val accountIdKey = randomString(6)
            val service = Accounts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.getBalance(accountIdKey)

            data.shouldNotBeNull()
            data.accountType.shouldBe(AccountType.MARGIN)
            data.description.shouldBe("NAOMI NAGATA")
            data.optionLevelValue.shouldBe(3)
            data.quoteModeValue.shouldBe(QuoteMode.QUOTE_REALTIME)
            data.accountMode.shouldBe(AccountMode.MARGIN)

            val cash = data.cash
            cash.fundsForOpenOrdersCash.shouldBe(0.0f)
            cash.moneyMktBalance.shouldBe(6934.52f)

            val balances = data.balances
            balances.cashAvailableForInvestment.shouldBe(7685.52f)
            balances.netCash.shouldBe(7685.52f)
            balances.marginBuyingPower.shouldBe(15371.04f)
            balances.marginBalance.shouldBe(37388.25f)
            balances.regtEquity.shouldBe(44322.77f)

            val openCalls = balances.openCalls
            openCalls.minEquityCall.shouldBe(0.0f)
            openCalls.fedCall.shouldBe(0.0f)
            openCalls.cashCall.shouldBe(0.0f)
            openCalls.houseCall.shouldBe(0.0f)

            val realTimeValues = balances.realTimeValues
            realTimeValues.totalAccountValue.shouldBe(37645.27f)
            realTimeValues.netMv.shouldBe(-6677.5f)
            realTimeValues.totalLongValue.shouldBeNull()

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/balance?instType=BROKERAGE&realTimeNAV=true")
        }
    }

    "list 50 transactions" {
        val path = Paths.get("apiResponses/accounts/list_transactions.json")

        createServer(path) {
            val accountIdKey = randomString(6)
            val service = Accounts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.listTransactions(accountIdKey)

            data.shouldNotBeNull()
            data.transactions.count().shouldBe(data.transactionCount)

            val first = data.transactions.first()
            first.transactionId.shouldBe(21048101297804L)
            first.accountId.shouldBe("45645298")
            first.transactionDate.shouldBe(Instant.ofEpochMilli(1613548800000L))
            first.postDate.shouldBe(Instant.ofEpochMilli(1613635200000L))
            first.amount.shouldBe(1138.42f)
            first.description.shouldContain("PALANTIR TECHNOLOGIES INC CL A")
            first.transactionType.shouldBe("Sold Short")

            val trade = first.trade
            trade.quantity.shouldBe(-3.0f)
            trade.price.shouldBe(3.8f)
            trade.settlementCurrency.shouldBe("USD")
            trade.paymentCurrency.shouldBe("USD")
            trade.fee.shouldBe(1.5f)
            trade.displaySymbol.shouldContain("PLTR")
            trade.settlementDate.shouldBe(Instant.ofEpochMilli(1613635200000))

            val strike = trade.strike!!
            strike.symbol.shouldBe("PLTR")
            strike.securityType!!.description.shouldBe("Option")
            strike.callPut.shouldBe(OptionType.PUT)
            strike.expiry.shouldBe(GregorianCalendar(2021, 3, 12))
            strike.price.shouldBe(29.0f)

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/transactions?count=50")
        }
    }

    "list transactions by date" {
        val path = Paths.get("apiResponses/accounts/list_transactions_by_date.json")

        createServer(path) {
            val accountIdKey = randomString(6)
            val service = Accounts(mockSession(), baseUrl = it.url(".").toString())
            val startDate = GregorianCalendar(2020, 9, 1)
            val endDate = GregorianCalendar(2020, 9, 3)
            val data = service.listTransactions(accountIdKey, startDate, endDate)

            data.shouldNotBeNull()
            data.transactions.count().shouldBe(data.transactionCount)

            // Sweeps have 2 transactions each
            data.transactions[0].transactionDate.shouldBe(Instant.ofEpochMilli(1601622000000)) // Oct 2, 2020
            data.transactions[2].transactionDate.shouldBe(Instant.ofEpochMilli(1601535600000)) // Oct 1, 2020

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/transactions?count=50&startDate=10012020&endDate=10032020")
        }
    }

    "list 5 transactions sorted ascending" {
        val path = Paths.get("apiResponses/accounts/list_transactions_sort_asc.json")

        createServer(path) {
            val accountIdKey = randomString(6)
            val service = Accounts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.listTransactions(accountIdKey, null, null, TransactionSortOrder.DESC, null, 5)

            data.shouldNotBeNull()
            data.transactions.count().shouldBe(data.transactionCount)

            data.transactions[0].transactionDate.shouldBe(Instant.ofEpochMilli(1611561600000)) // Jan 25, 2021

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/transactions?count=5&sortOrder=DESC")
        }
    }

    "get transaction details" {
        val path = Paths.get("apiResponses/accounts/get_transaction_details.json")

        createServer(path) {
            val accountIdKey = randomString(6)
            val transactionId = nextLong()
            val service = Accounts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.getTransaction(accountIdKey, transactionId)
            data.shouldNotBeNull()
            data.description.shouldBe("MegaCorp IRA")
            data.transactionDate.shouldBe(Instant.ofEpochMilli(1630652400000)) // 2021-09-03

            it.takeRequest().path.shouldBe("/v1/accounts/$accountIdKey/transactions/$transactionId")
        }
    }
})