package com.seansoper.batil.connectors.etrade

class Accounts(session: Session,
               production: Boolean? = null,
               verbose: Boolean? = null,
               baseUrl: String? = null): Service(session, production, verbose, baseUrl) {

    fun list(): List<Account>? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getAccounts().execute()

        return response.body()?.response?.accountRoot?.accounts
    }

    fun getBalance(accountIdKey: String): AccountBalance? {
        val service = createClient(AccountsApi::class.java)
        val response = service.getBalance(accountIdKey).execute()

        return response.body()?.response
    }
}
