import TestHelper.MockHelper.createServer
import TestHelper.MockHelper.mockSession
import com.seansoper.batil.connectors.etrade.Alerts
import com.seansoper.batil.connectors.etrade.Status
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths
import java.time.Instant

class AlertsTest: StringSpec({

    "list alerts" {
        val path = Paths.get("apiResponses/alerts/list.json")

        createServer(path) {
            val service = Alerts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.list()

            data.shouldNotBeNull()
            data.totalAlerts.shouldBe(96)
            data.alerts.count().shouldBe(25)

            val first = data.alerts.first()
            first.subject.shouldContain("EDIT")
            first.status.shouldBe(Status.UNREAD)
            first.createTime.shouldBe(Instant.ofEpochSecond(1631302123)) // 9-10-2021

            val last = data.alerts.last()
            last.subject.shouldContain("SPCE")
            last.status.shouldBe(Status.UNREAD)
            last.createTime.shouldBe(Instant.ofEpochSecond(1630008354)) // 8-26-2021

            it.takeRequest().path.shouldBe("/v1/user/alerts")
        }
    }

    "alert details" {
        val path = Paths.get("apiResponses/alerts/details.json")

        createServer(path) {
            val alertId = 886
            val service = Alerts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.get(alertId)

            data.shouldNotBeNull()
            data.id.shouldBe(alertId)
            data.subject.shouldContain("EDIT")
            data.createTime.shouldBe(Instant.ofEpochSecond(1631302123)) // 9-10-2021
            data.readTime.shouldBe(Instant.ofEpochSecond(1631387366)) // 9-11-2021
            data.symbol.shouldBe("EDIT--211001P00055000") // See https://www.optionstaxguy.com/option-symbols-osi

            it.takeRequest().path.shouldBe("/v1/user/alerts/$alertId")
        }
    }

    "delete single alert" {
        val path = Paths.get("apiResponses/alerts/delete_single.json")

        createServer(path) {
            val alertId = 886
            val service = Alerts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.delete(alertId)

            data.shouldNotBeNull()
            data.succeeded.shouldBe(true)
            data.failedAlerts.shouldBeNull()

            it.takeRequest().path.shouldBe("/v1/user/alerts/$alertId")
        }
    }

    "delete many alerts" {
        val path = Paths.get("apiResponses/alerts/delete_multiple.json")

        createServer(path) {
            val alertsId = listOf(886, 907, 908)
            val service = Alerts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.delete(alertsId)

            data.shouldNotBeNull()
            data.succeeded.shouldBe(false)
            data.failedAlerts.shouldNotBeNull()
            data.failedAlerts!!.size.shouldBe(2)
            data.failedAlerts!!.shouldContain(907)
            data.failedAlerts!!.shouldContain(908)

            it.takeRequest().path.shouldBe("/v1/user/alerts/${alertsId.joinToString(",")}")
        }
    }

    "delete alerts failure" {
        val path = Paths.get("apiResponses/alerts/delete_failure.xml")

        createServer(path, header = "Content-Type" to "application/xml", code = 500) {
            val alertsId = listOf(907, 908)
            val service = Alerts(mockSession(), baseUrl = it.url(".").toString())
            val data = service.delete(alertsId)

            data.shouldBeNull()

            it.takeRequest().path.shouldBe("/v1/user/alerts/${alertsId.joinToString(",")}")
        }
    }
})