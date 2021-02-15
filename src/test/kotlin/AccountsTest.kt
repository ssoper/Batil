import TestHelper.MockHelper.createServer
import TestHelper.MockHelper.mockSession
import com.seansoper.batil.connectors.etrade.AccountType
import com.seansoper.batil.connectors.etrade.Accounts
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

})