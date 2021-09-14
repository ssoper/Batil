package com.seansoper.batil

import com.seansoper.batil.config.GlobalConfig
import com.seansoper.batil.config.RuntimeConfig
import com.seansoper.batil.connectors.etrade.Authorization
import com.seansoper.batil.connectors.etrade.Market
import java.util.GregorianCalendar

/**
 * @suppress
 */
class Samples {

    fun getOptionsChain() {
        val runtime = RuntimeConfig.default()
        val configuration = GlobalConfig.parse(runtime)
        val client = Authorization(configuration, runtime.production, runtime.verbose)
        val session = client.renewSession() ?: client.createSession()
        val service = Market(session, runtime.production, runtime.verbose)
        val date = OptionsCalendar.nextMonthly()
        val result = service.optionChains("AAPL", GregorianCalendar(date.year, date.month.value, date.dayOfMonth), 131f, 1)

        result?.let {
            println(it)
        }
    }
}
