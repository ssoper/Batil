<p align="center">
  <img src="https://github.com/ssoper/Batil/raw/master/gh/logo.png" width="400" alt="Batil Logo">
</p>

[![tests](https://github.com/ssoper/Batil/actions/workflows/build.yml/badge.svg)](https://github.com/ssoper/Batil/actions)
[![codecov](https://codecov.io/gh/ssoper/Batil/branch/master/graph/badge.svg?token=AX0EVTOJCS)](https://codecov.io/gh/ssoper/Batil)
[![Maven Central](https://img.shields.io/maven-central/v/com.seansoper/batil)](https://mvnrepository.com/artifact/com.seansoper)
[![Kotlin](https://img.shields.io/badge/kotlin-1.5.30-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

Make your brokerage work for you.

## Goals

* Connect to your preferred broker in Kotlin or Java 💁
* Provide a single interface for accessing multiple brokers’ APIs 🏪
* Accelerate the development of algorithmic trading for the JVM 💰

## Supported Brokers

* [E\*TRADE](https://etrade.com/)

## Setup

### E*TRADE

1. [Retrieve your credentials](#Credentials)
2. [Setup Docker](#Docker)
3. [Verify your credentials](#Verify)
4. [Other Commands](#other-commands)
5. [Troubleshooting](#Troubleshooting)

#### Credentials

You’ll need to request both a sandbox and production API consumer key and secret from the API team.

* Sign into your E\*TRADE account and head over to Customer Service ➡ Message Center ➡ Contact Us. From there select the account you want to associate with your API key. For the subject, select `API Sandbox Auto` and for the topic select `Sandbox Key`. Expect to hear back within a few hours.
* To access the production API you’ll need to send a signed copy of the [Developer Agreement](https://content.etrade.com/etrade/estation/pdf/APIDeveloperAgreement.pdf) to etradeapi@etrade.com.

#### Docker

Docker is used to access a Chromium instance that can login to the E\*TRADE website to retrieve the necessary OAuth keys.

* [Download Docker](https://www.docker.com/products/docker-desktop)
* Run the following command to start the container:

```bash
docker container run -d -p 9222:9222 zenika/alpine-chrome --no-sandbox --remote-debugging-address=0.0.0.0 --remote-debugging-port=9222 --user-agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36" about:blank
```

* If you are on Apple Silicon (M1) you should use this image instead which was built with arm64:

```bash
docker container run -d -p 9222:9222 avidtraveler/alpine-chrome --no-sandbox --remote-debugging-address=0.0.0.0 --remote-debugging-port=9222 --user-agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36" about:blank
```

#### Verify

* You can can clone the project locally and build the demo E\*TRADE client (check the `build/libs` dir).

```bash
./gradlew fatJar
```

* Using the [sample provided](batil.sample.yaml), add a `batil.yaml` to the directory where you intend to run the JAR file. Substitute the default values with the correct values.
* **_Important_** Ensure you never check your version of `batil.yaml` into git.
* Verify your account.

```bash
java -jar Batil-etrade.jar verify
```

* By default the app runs in sandbox mode. Add the `--production` switch to use in production.

```bash
java -jar Batil-etrade.jar verify --production
```

#### Other Commands

For the full list of available options use the `--help` switch.

##### List Accounts

```bash
% java -jar Batil-etrade.jar list_accounts --production

Account ID (47246378)
Key: -i07qS52YOXHWSjf8hvZPA
Type: INDIVIDUAL
Name:
Status: ACTIVE
Description: Individual Brokerage
```

##### Get Balances

```bash
% java -jar Batil-etrade.jar get_balances -i07qS52YOXHWSjf8hvZPA --production

Account ID Key (-i07qS52YOXHWSjf8hvZPA)
Net cash: 3470.154
Cash balance: 0.0
Margin balance: -1743.5747
Cash buying power: 3470.154
Margin buying power: 26233.848
Cash available for investment: 0.0
Cash available for withdrawal: 0.0
```

##### Lookup Tickers

```bash
% java -jar Batil-etrade.jar lookup pltr tsla clov --production

PALANTIR TECHNOLOGIES INC CL A
Last bid: 24.35
Earnings per share: -0.7414
Total volume: 26106710

TESLA INC COM
Last bid: 818.32
Earnings per share: 1.9124
Total volume: 12247170

CLOVER HEALTH INVESTMENTS CORP COM CL A
Last bid: 8.12
Earnings per share: -0.9715
Total volume: 18767897
```

#### Troubleshooting

* [Connecting to the E\*TRADE API](https://seansoper.com/blog/connecting_etrade.html)

## Integration

Before you bring Batil into your own project you’ll want to first [retrieve your credentials](#Credentials) from your broker and
[setup Docker](#docker). Then add the dependency to your Gradle file.

**build.gradle.kts**
```kotlin
implementation("com.seansoper:batil:1.0.0")
```

You can then use the full-suite of service endpoints.

```kotlin
val verbose = true
val production = true

val clientConfig = ClientConfig(Paths.get("/path/to/batil.yaml"), verbose, production)
val globalConfig = GlobalConfig.parse(clientConfig)
val client = Authorization(globalConfig, production, verbose)
val session = client.renewSession() ?: client.createSession()
val accounts = Accounts(session, production, verbose)

accounts.list()?.let {
    it.first().accountIdKey?.let { accountIdKey ->
        val service = Orders(session, runtime.production, runtime.verbose)
        val previewRequest = buyEquityLimit("PLTR", 21f, 100)

        service.createPreview(accountIdKey, previewRequest)?.let { previewOrderResponse ->
            service.placeOrder(accountIdKey, previewRequest, previewOrderResponse)?.let {
                println("Purchased 100 shares of PLTR at $21")
                println(it)
            }
        }
    }
}
```

More examples of usage can be found [here](https://github.com/ssoper/Batil/blob/master/src/main/kotlin/Samples.kt). Full documentation [available here](https://seansoper.com/Batil/).

## So What’s a Batil Anyways?

The [Batil](https://www.naval-encyclopedia.com/medieval-ships/) was a coastal ship in use for hundreds of years up until the 1950s. Serving primarily as a merchant vessel, it was ubiquitous throughout South Asia and the Middle East helping to create a resilient trade network.
