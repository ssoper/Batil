package com.seansoper.batil

import com.seansoper.batil.connectors.Etrade
import com.seansoper.batil.connectors.EtradeAuthResponse
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.nio.file.Paths
import java.security.KeyStore
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess
import javax.crypto.KeyGenerator




object Core {
    @JvmStatic fun main(args: Array<String>) {
        val cli = CommandLineParser(args)

        if (cli.shouldShowHelp) {
            cli.showHelp()
            exitProcess(0)
        }

        val parsed = try {
            cli.parse()
        } catch (exception: ConfigFileNotFound) {
            println("❌ ${exception.localizedMessage}")
            cli.showHelp()
            exitProcess(1)
        }

        if (parsed.verbose) {
            println("Verbose set to ${parsed.verbose}")
            println("Config path set to ${parsed.pathToConfigFile}")
        }

        val configuration = try {
            IngestConfiguration(parsed).parse()
        } catch (exception: ConfigFileInvalid) {
            println(exception)
            exitProcess(1)
        }

        val auth = EtradeAuthResponse("3N4jVTyisOWT272T3QVIsDCEgL7iyAd71Vgm98FFy4w=",
                                      "Plm7K2ApiPacgM1KJk7ZEYba6o7hJwV8VyCFmK46OAg=")
        val verifier = "3D8ZO"
        saveToken(auth, verifier)

        /*
        val client = Etrade(configuration, parsed.production, parsed.verbose)
        val requestToken = client.getRequestToken()
        val verifier = client.getVerifierCode(requestToken.accessToken)

        if (parsed.verbose) {
            println("Verifier code is $verifier")
        }

        val oauthToken = client.getAccessToken(requestToken, verifier, true)

        if (parsed.verbose) {
            oauthToken.apply {
                println("Access OAuth token is $accessToken")
                println("Access OAuth secret is $accessSecret")
            }
        }

        client.renewAccessToken(oauthToken)

        // val data = client.ticker("AAPL", oauthToken, verifier)
        // val data = client.lookup("Game", oauthToken, verifier)
        // val data = client.optionChains("AAPL", oauthToken, verifier)
        val data = client.optionChains("AAPL", GregorianCalendar(2021, 2, 5), 131f, 1, oauthToken, verifier)
        data?.let {
            print(it)
        }

         */
    }

    private fun saveToken(authResponse: EtradeAuthResponse, verifier: String) {
        // check if directory exists, if not create it
        val dirPath = Paths.get(System.getProperty("user.home"), ".batil")

        if (!dirPath.toFile().exists()) {
            dirPath.toFile().mkdir()
        }

        val keyFilePath = Paths.get(dirPath.toString(), "etrade")
        val secretKey = SecretKeySpec(authResponse.accessSecret.toByteArray(), "AES")

        // base64 encoded secret key
        // val ek1 = Base64.getEncoder().encodeToString(secretKey.encoded)

        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        val pword = "itsasekrit".toCharArray()
        val p1 = KeyStore.PasswordProtection(pword)
//        val fis = FileInputStream(keyFilePath.toFile())
        ks.load(null, pword)

        // val entry = ks.getEntry("secret", p1) as KeyStore.PrivateKeyEntry
        // val key = entry.privateKey


        val s2entry = KeyStore.SecretKeyEntry(secretKey)
        ks.setEntry("secret", s2entry, p1)

        val stream = FileOutputStream(keyFilePath.toFile())
        stream.use {
            //it.write(ek1.toByteArray())
            ks.store(it, pword)
        }

/*
        val keys = "${authResponse.accessToken}|${authResponse.accessSecret}"
        val bytes = keys.byteInputStream()
        val stream = FileOutputStream(keyFilePath.toFile())
        stream.write(keys.toByteArray())
*/
        // object serialization
/*
        val s2 = ByteArrayOutputStream()
        val o2 = ObjectOutputStream(s2)
        o2.writeObject(authResponse)
        o2.flush()

        stream.write(s2.toByteArray())
*/
        // if key file exists, delete it
        // create new key file with keys
    }
}
