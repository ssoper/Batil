import TestHelper.MockHelper.createServer
import TestHelper.MockHelper.mockSession
import TestHelper.PathHelper.randomString
import com.seansoper.batil.connectors.etrade.AccountMode
import com.seansoper.batil.connectors.etrade.AccountType
import com.seansoper.batil.connectors.etrade.Accounts
import com.seansoper.batil.connectors.etrade.QuoteMode
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths
import java.time.Instant

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
})