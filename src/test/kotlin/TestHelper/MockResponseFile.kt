package TestHelper

import java.io.InputStreamReader

class MockResponseFile(path: String) {
    val content: String

    init {
        val reader = InputStreamReader(this.javaClass.classLoader.getResourceAsStream(path)!!)
        content = reader.readText()
        reader.close()
    }
}