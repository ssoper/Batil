package TestHelper.MockHelper

import com.seansoper.batil.brokers.etrade.auth.Session
import io.kotlintest.matchers.types.shouldNotBeNull
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.InputStreamReader
import java.nio.file.Path

data class MockHelper(val path: String) {
    val content: String

    init {
        val reader = InputStreamReader(this.javaClass.classLoader.getResourceAsStream(path)!!)
        content = reader.readText()
        reader.close()
    }
}

fun createServer(
    content: String,
    header: Pair<String, String> = "Content-Type" to "application/json",
    code: Int = 200,
    test: (server: MockWebServer) -> Unit
) {
    val server = MockWebServer()
    server.start()

    content.shouldNotBeNull()

    val response = MockResponse()
        .addHeader(header.first, header.second)
        .setResponseCode(code)
        .setBody(content)
    server.enqueue(response)

    test(server)
    server.close()
}

fun createServer(
    path: Path,
    header: Pair<String, String> = "Content-Type" to "application/json",
    code: Int = 200,
    test: (server: MockWebServer) -> Unit
) {
    val content = MockHelper(path.toString()).content
    createServer(content, header, code, test)
}

fun mockSession(): Session {
    return Session("consumerKey", "consumerSecret", "token", "secret", "code")
}
