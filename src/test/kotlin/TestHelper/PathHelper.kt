package TestHelper.PathHelper

import java.nio.file.Path
import java.nio.file.Paths

fun randomString(length: Int = 15): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun tmpDirPath(): Path {
    return Paths.get("/tmp", "batil", randomString())
}

fun getPaths(): Triple<Path, Path, Path> {
    val dirPath = tmpDirPath()

    return Triple(
        dirPath,
        Paths.get(dirPath.toString(), "key.store"),
        Paths.get(dirPath.toString(), "key.password")
    )
}
