package com.seansoper.batil.brokers.etrade.auth

import com.seansoper.batil.config.Chromium
import io.reactivex.Single
import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.api.dom.GetAttributesRequest
import pl.wendigo.chrome.api.dom.GetBoxModelRequest
import pl.wendigo.chrome.api.dom.GetDocumentRequest
import pl.wendigo.chrome.api.dom.Node
import pl.wendigo.chrome.api.dom.QuerySelectorRequest
import pl.wendigo.chrome.api.input.DispatchKeyEventRequest
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
        CONFIRM_MFA_PHONE,
        CONFIRM_MFA_CODE,
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

                await {
                    target.Page.enable()
                }

                target.Network.loadingFinished().subscribe(
                    { value -> logMessage("Loading event finished: $value") },
                    { throwable -> logMessage("Loading event error: $throwable") },
                    { logMessage("Loading event unsubscribed") }
                )

                if (verbose) {
                    await {
                        target.Network.enable(EnableRequest())
                        target.Browser.getVersion().flatMap {
                            logMessage("User agent: ${it.userAgent}")
                            Single.just(it)
                        }
                    }

                    logMessage("Navigating to $url")
                }

                await { navigateTo(url, target) }
                val authNode = await { getRootNode(target) }

                await { clickElement(authNode, "input[id='USER']", target) }
                for (char in username) {
                    await { typeCharacter(char, target) }
                    Thread.sleep((100..150).random().toLong())
                }

                await { clickElement(authNode, "input[id='password']", target) }
                for (char in password) {
                    await { typeCharacter(char, target) }
                    Thread.sleep((100..150).random().toLong())
                }

                if (verbose) {
                    await { saveScreenshot(Screenshot.AUTHORIZATION, target) }
                }

                await { clickElement(authNode, "button[id='mfaLogonButton']", target) }
                Thread.sleep(delay)

                val sendMfaCodeButton = await { elementExists(authNode, "button[id='sendOTPCodeBtn']", target) }

                if (sendMfaCodeButton) {
                    if (verbose) {
                        await { saveScreenshot(Screenshot.CONFIRM_MFA_PHONE, target) }
                    }

                    val sendMfaNode = await { getRootNode(target) }
                    val mfaPhoneNumber = await { getValue(sendMfaNode, "#application > .container .tiny-header.label", target, "innerText") }
                    await { clickElement(sendMfaNode, "button[id='sendOTPCodeBtn']", target) }

                    print("\nMFA code sent to $mfaPhoneNumber, enter the code received: ")
                    val mfaCode = readLine() ?: throw Error("No MFA code provided")
                    println()

                    Thread.sleep(delay)
                    val confirmMfaNode = await { getRootNode(target) }
                    await { clickElement(confirmMfaNode, "input[id='verificationCode']", target) }
                    for (char in mfaCode) {
                        await { typeCharacter(char, target) }
                        Thread.sleep((100..150).random().toLong())
                    }

                    await { clickElement(confirmMfaNode, "input[id='saveDevice']", target) }

                    if (verbose) {
                        await { saveScreenshot(Screenshot.CONFIRM_MFA_CODE, target) }
                    }

                    val submitButtonSelector = "#application > div > div.row > div > div:nth-child(3) > div:nth-child(4) > button"
                    await { clickElement(confirmMfaNode, submitButtonSelector, target) }
                    Thread.sleep(delay)
                }

                if (verbose) {
                    await { saveScreenshot(Screenshot.ACCEPT_TOS, target) }
                }

                // If redirected to page asking for MFA phone
                // 1. Get value of phone number
                // 2. Click Send Code button
                // 3. Print phone number
                // 4. Get MFA code from readLine
                // 5. Input user supplied MFA code, click yes on save device
                // 6. Press submit

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

    private fun typeCharacter(character: Char, target: Target): Single<ResponseFrame> {
        return target.Input.dispatchKeyEvent(
            DispatchKeyEventRequest(
                "keyDown",
                text = character.toString()
            )
        ).flatMap {
            target.Input.dispatchKeyEvent(
                DispatchKeyEventRequest(
                    "keyUp",
                    text = character.toString()
                )
            )
        }
    }

    private fun elementExists(rootNode: Node, selector: String, target: Target): Single<Boolean> {
        return try {
            target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap {
                Single.just(true)
            }
        } catch (exception: pl.wendigo.chrome.protocol.RequestFailed) {
            Single.just(false)
        }
    }

    private fun clickElement(rootNode: Node, selector: String, target: Target): Single<ResponseFrame> {
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
        }
    }

    private fun getValue(rootNode: Node, selector: String, target: Target, attribute: String = "value"): Single<String> {
        return target.DOM.querySelector(QuerySelectorRequest(rootNode.nodeId, selector)).flatMap { (element) ->
            target.DOM.getAttributes(GetAttributesRequest(element)).flatMap { (attributes) ->
                attributes.indexOf(attribute).let {
                    val found = attributes[it + 1].trim()
                    Single.just(found)
                }
            }
        }
    }
}

class EtradeBrowserAuthNoConnection(chromiumUrl: String) : Exception("Could not connect to chromium instance at $chromiumUrl")
