package com.seansoper.batil.connectors

import com.seansoper.batil.Chromium
import io.reactivex.Single
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.api.dom.*
import pl.wendigo.chrome.api.input.DispatchMouseEventRequest
import pl.wendigo.chrome.api.input.MouseButton
import pl.wendigo.chrome.api.network.EnableRequest
import pl.wendigo.chrome.api.page.CaptureScreenshotRequest
import pl.wendigo.chrome.api.page.FrameStoppedLoadingEvent
import pl.wendigo.chrome.api.page.NavigateRequest
import pl.wendigo.chrome.await
import pl.wendigo.chrome.protocol.ResponseFrame
import pl.wendigo.chrome.targets.Target
import java.net.URLEncoder
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class EtradeBrowserAuth(key: String,
                        token: String,
                        private val username: String,
                        private val password: String,
                        private val configuration: Chromium,
                        private val verbose: Boolean = false) {

    private val url = "https://us.etrade.com/e/t/etws/authorize?key=${key.encodeUtf8()}&token=${token.encodeUtf8()}"
    private val delay = (configuration.delay*1000).toLong()

    private fun String.encodeUtf8() = URLEncoder.encode(this, "UTF-8").replace("+", "%2B")

    private val tmpDirPath: Path by lazy {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..15)
                .map { kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")

        Paths.get("/tmp", "batil", randomString)
    }

    private enum class Screenshot {
        AUTHORIZATION,
        ACCEPT_TOS,
        VERIFIER_CODE
    }

    fun retrieve(): String {
        val chromeUrl = "${configuration.ip}:${configuration.port}"

        if (verbose) {
            println("Using chromium instance at $chromeUrl")
            println("Screenshots will be saved to $tmpDirPath")
            tmpDirPath.toFile().mkdirs()
        }

        val chrome = try {
            Browser.builder()
                    .withAddress(chromeUrl)
                    .build()
        } catch (_: java.net.ConnectException) {
            throw EtradeBrowserAuthNoConnection(chromeUrl)
        }

        return chrome.use { browser ->
            browser.target("about:blank").use { target ->

                await {
                    target.Page.enable()
                }

                target.Network.loadingFinished().subscribe(
                    { value -> println("Loading event finished: $value") },
                    { throwable -> println("Loading event error: $throwable") },
                    { println("Loading event unsubscribed") }
                )

                if (verbose) {
                    await {
                        target.Network.enable(EnableRequest())
                        target.Browser.getVersion().flatMap {
                            println("User agent: ${it.userAgent}")
                            Single.just(it)
                        }
                    }

                    println("Navigating to $url")
                }

                await { navigateTo(url, target) }
                val authNode = await { getRootNode(target) }

                await { fillValue(authNode, "input[name='USER']", username, target) }
                await { fillValue(authNode, "input[name='PASSWORD']", password, target) }

                if (verbose) {
                    await { saveScreenshot(Screenshot.AUTHORIZATION, target) }
                }

                await { clickElement(authNode, "#logon_button", target) }
                Thread.sleep(delay)

                if (verbose) {
                    await { saveScreenshot(Screenshot.ACCEPT_TOS, target) }
                }

                val tosNode = await { getRootNode(target) }
                await { clickElement(tosNode, "input[value='Accept']", target) }
                Thread.sleep(delay)

                if (verbose) {
                    await { saveScreenshot(Screenshot.VERIFIER_CODE, target) }
                }

                val verifierNode = await { getRootNode(target) }
                await { getValue(verifierNode, "div > input[type='text']", target) }
            }
        }
    }

    private fun navigateTo(url: String, target: Target): Single<FrameStoppedLoadingEvent> {
        return target.Page.navigate(NavigateRequest(url = url)).flatMap { (frameId) ->
            target.Page.frameStoppedLoading().filter {
                it.frameId == frameId
            }.take(1).singleOrError()
        }
    }

    private fun saveScreenshot(screenshot: Screenshot, target: Target): Single<Path> {
        return target.Page.captureScreenshot(CaptureScreenshotRequest()).flatMap { (data) ->
            val byteArray = Base64.getDecoder().decode(data)
            val filename = "${screenshot.ordinal}_${screenshot.name}.png"
            val path = Paths.get(tmpDirPath.toString(), filename)
            path.toFile().writeBytes(byteArray)
            Single.just(path)
        }
    }

    private fun getRootNode(target: Target): Single<Node> {
        return target.DOM.getDocument(GetDocumentRequest(-1)).flatMap { (node) ->
            Single.just(node)
        }
    }

    private fun fillValue(rootNode: Node, selector: String, value: String, target: Target): Single<ResponseFrame> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (fieldUsername) ->
            target.DOM.setAttributeValue(SetAttributeValueRequest(fieldUsername, "value", value)).flatMap {
                Single.just(it)
            }
        }
    }

    private fun clickElement(rootNode: Node, selector: String, target: Target): Single<ResponseFrame> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (button) ->
            target.DOM.getBoxModel(GetBoxModelRequest(button)).flatMap { (box) ->
                val coordinates = Pair(box.content[0]+1, box.content[1]+1)

                target.Input.dispatchMouseEvent(
                        DispatchMouseEventRequest("mousePressed",
                                coordinates.first,
                                coordinates.second,
                                button = MouseButton.LEFT,
                                clickCount = 1)).flatMap {
                    target.Input.dispatchMouseEvent(DispatchMouseEventRequest("mouseReleased",
                            coordinates.first,
                            coordinates.second,
                            button = MouseButton.LEFT))
                }
            }
        }
    }

    private fun getValue(rootNode: Node, selector: String, target: Target): Single<String> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (element) ->
            target.DOM.getAttributes(GetAttributesRequest(element)).flatMap { (attributes) ->
                attributes.indexOf("value").let {
                    val found = attributes[it+1].trim()
                    Single.just(found)
                }
            }
        }
    }
}

class EtradeBrowserAuthNoConnection(chromiumUrl: String): Exception("Could not connect to chromium instance at $chromiumUrl")
