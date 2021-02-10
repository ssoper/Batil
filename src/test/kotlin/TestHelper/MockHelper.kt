package TestHelper.MockHelper

import io.kotlintest.matchers.types.shouldNotBeNull
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.InputStreamReader

data class MockHelper(val path: String) {
    val content: String

    init {
        val reader = InputStreamReader(this.javaClass.classLoader.getResourceAsStream(path)!!)
        content = reader.readText()
        reader.close()
    }
}

fun createServer(pathToContent: String? = null,
                 header: Pair<String, String> = "Content-Type" to "application/json",
                 code: Int = 200,
                 test: (server: MockWebServer) -> Unit) {
    val server = MockWebServer()
    server.start()

    pathToContent?.let {
        val content = MockHelper(it).content
        content.shouldNotBeNull()

        val response = MockResponse()
            .addHeader(header.first, header.second)
            .setResponseCode(code)
            .setBody(content)
        server.enqueue(response)
    }

    test(server)
    server.close()
}