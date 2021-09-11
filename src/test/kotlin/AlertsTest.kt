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

})