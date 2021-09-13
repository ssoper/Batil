<p align="center"><img src="https://github.com/ssoper/Batil/raw/master/gh/batil.png" width="350" alt="Batil Logo"></p>

![tests](https://github.com/ssoper/Batil/actions/workflows/build.yml/badge.svg)

# Batil

Connect to your preferred broker in functional Kotlin.

## Goals

* Provide a single interface for accessing multiple brokers’ APIs 🏪
* Create a Maven package for ease of inclusion in other projects 🛍

## Supported Brokers

* [E\*TRADE](https://etrade.com/)

## Setup

* [Download Docker](https://www.docker.com/products/docker-desktop)
* Run the following command to start the container:

        docker container run -d -p 9222:9222 zenika/alpine-chrome --no-sandbox --remote-debugging-address=0.0.0.0 --remote-debugging-port=9222 --user-agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36" about:blank

* Note that if you are on Apple Silicon (M1) you should use this image built with arm64:

        docker container run -d -p 9222:9222 avidtraveler/alpine-chrome --no-sandbox --remote-debugging-address=0.0.0.0 --remote-debugging-port=9222 --user-agent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36" about:blank

* Using the sample provided, add a `batil.yaml` to the directory where you intend to run the JAR file substituting with the correct values.
* Now you can run the JAR file.

        java -jar path/to/Batil.main.jar

* By default the app runs in sandbox mode. Add the `-production` switch to use in production.

        java -jar path/to/Batil.main.jar -production

* For the full list of available options use the `-help` switch.

## So What’s a Batil Anyways?

The [Batil](https://www.naval-encyclopedia.com/medieval-ships/) was a coastal ship in use for hundreds of years up until the 1950s. Serving primarily as a merchant vessel, it was ubiquitous throughout South Asia and the Middle East helping to create a resilient trade network.
