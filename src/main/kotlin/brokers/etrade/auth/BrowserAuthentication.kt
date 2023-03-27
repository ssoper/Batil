package com.seansoper.batil.brokers.etrade.auth

import com.seansoper.batil.config.Chromium
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.api.dom.GetAttributesRequest
import pl.wendigo.chrome.api.dom.GetBoxModelRequest
import pl.wendigo.chrome.api.dom.GetDocumentRequest
import pl.wendigo.chrome.api.dom.Node
import pl.wendigo.chrome.api.dom.QuerySelectorRequest
import pl.wendigo.chrome.api.dom.SetAttributeValueRequest
import pl.wendigo.chrome.api.input.DispatchMouseEventRequest
import pl.wendigo.chrome.api.input.MouseButton
import pl.wendigo.chrome.api.network.EnableRequest
import pl.wendigo.chrome.api.page.CaptureScreenshotRequest
import pl.wendigo.chrome.api.page.FrameStoppedLoadingEvent
import pl.wendigo.chrome.api.page.NavigateRequest
import pl.wendigo.chrome.awaitMany
import pl.wendigo.chrome.protocol.websocket.RequestResponseFrame
import pl.wendigo.chrome.targets.Target
import java.net.URLEncoder
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64

class BrowserAuthentication(
    key: String,
    token: String,
    private val username: String,
    private val password: String,
    private val configuration: Chromium,
    private val verbose: Boolean = false
) {

    private val url = "https://us.etrade.com/e/t/etws/authorize?key=${key.encodeUtf8()}&token=${token.encodeUtf8()}"
    private val delay = (configuration.delay * 1000).toLong()

    private fun String.encodeUtf8() = URLEncoder.encode(this, "UTF-8").replace("+", "%2B")

    private val tmpDirPath: Path by lazy {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
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

    fun logMessage(message: String) {
        println("[Chromium] $message")
    }

    fun retrieve(): String {
        val chromeUrl = "${configuration.ip}:${configuration.port}"

        if (verbose) {
            logMessage("Using instance at $chromeUrl")
            logMessage("Screenshots will be saved to $tmpDirPath")
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

                awaitMany {
                    target.Page.enable().toFlowable()
                }

                target.Network.loadingFinished().subscribe(
                    { value -> logMessage("Loading event finished: $value") },
                    { throwable -> logMessage("Loading event error: $throwable") },
                    { logMessage("Loading event unsubscribed") }
                )

                if (verbose) {
                    awaitMany {
                        target.Network.enable(EnableRequest())
                        target.Browser.getVersion().flatMap {
                            logMessage("User agent: ${it.userAgent}")
                            Single.just(it)
                        }.toFlowable()
                    }

                    logMessage("Navigating to $url")
                }

                awaitMany { navigateTo(url, target) }
                val authNode = awaitMany { getRootNode(target) }.first()

                awaitMany { fillValue(authNode, "input[name='USER']", username, target) }
                awaitMany { fillValue(authNode, "input[name='PASSWORD']", password, target) }

                if (verbose) {
                    awaitMany { saveScreenshot(Screenshot.AUTHORIZATION, target) }
                }

                awaitMany { clickElement(authNode, "#logon_button", target) }
                Thread.sleep(delay)

                if (verbose) {
                    awaitMany { saveScreenshot(Screenshot.ACCEPT_TOS, target) }
                }

                val tosNode = awaitMany { getRootNode(target) }.first()
                awaitMany { clickElement(tosNode, "input[value='Accept']", target) }
                Thread.sleep(delay)

                if (verbose) {
                    awaitMany { saveScreenshot(Screenshot.VERIFIER_CODE, target) }
                }

                val verifierNode = awaitMany { getRootNode(target) }.first()
                awaitMany { getValue(verifierNode, "div > input[type='text']", target) }.first()
            }
        }
    }

    private fun navigateTo(url: String, target: Target): Flowable<FrameStoppedLoadingEvent> {
        return target.Page.navigate(NavigateRequest(url = url)).flatMap { (frameId) ->
            target.Page.frameStoppedLoading().filter {
                it.frameId == frameId
            }.take(1).singleOrError()
        }.toFlowable()
    }

    private fun saveScreenshot(screenshot: Screenshot, target: Target): Flowable<Path> {
        return target.Page.captureScreenshot(CaptureScreenshotRequest()).flatMap { (data) ->
            val byteArray = Base64.getDecoder().decode(data)
            val filename = "${screenshot.ordinal}_${screenshot.name}.png"
            val path = Paths.get(tmpDirPath.toString(), filename)
            path.toFile().writeBytes(byteArray)
            Single.just(path)
        }.toFlowable()
    }

    private fun getRootNode(target: Target): Flowable<Node> {
        return target.DOM.getDocument(GetDocumentRequest(-1)).flatMap { (node) ->
            Single.just(node)
        }.toFlowable()
    }

    private fun fillValue(rootNode: Node, selector: String, value: String, target: Target): Flowable<RequestResponseFrame> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (fieldUsername) ->
            target.DOM.setAttributeValue(SetAttributeValueRequest(fieldUsername, "value", value)).flatMap {
                Single.just(it)
            }
        }.toFlowable()
    }

    private fun clickElement(rootNode: Node, selector: String, target: Target): Flowable<RequestResponseFrame> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (button) ->
            target.DOM.getBoxModel(GetBoxModelRequest(button)).flatMap { (box) ->
                val coordinates = Pair(box.content[0] + 1, box.content[1] + 1)

                target.Input.dispatchMouseEvent(
                    DispatchMouseEventRequest(
                        "mousePressed",
                        coordinates.first,
                        coordinates.second,
                        button = MouseButton.LEFT,
                        clickCount = 1
                    )
                ).flatMap {
                    target.Input.dispatchMouseEvent(
                        DispatchMouseEventRequest(
                            "mouseReleased",
                            coordinates.first,
                            coordinates.second,
                            button = MouseButton.LEFT
                        )
                    )
                }
            }
        }.toFlowable()
    }

    private fun getValue(rootNode: Node, selector: String, target: Target): Flowable<String> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (element) ->
            target.DOM.getAttributes(GetAttributesRequest(element)).flatMap { (attributes) ->
                attributes.indexOf("value").let {
                    val found = attributes[it + 1].trim()
                    Single.just(found)
                }
            }
        }.toFlowable()
    }
}

class EtradeBrowserAuthNoConnection(chromiumUrl: String) : Exception("Could not connect to chromium instance at $chromiumUrl")
