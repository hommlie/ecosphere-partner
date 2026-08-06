package com.ecosphere.partner.core.network

import android.util.Log
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.model.Tokens
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val tokenStore: TokenStore,
    private val authApi: AuthApiInterface
) : Authenticator {

    private val refreshMutex = Mutex()

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        if (responseCount(response) >= 2) {
            clearSession()
            return null
        }

        val requestToken = response.request.header(
            NetworkConstants.HEADER_AUTHORIZATION
        )

        return runBlocking {

            refreshMutex.withLock {

                // Someone else already refreshed while we were waiting
                val latestAccessToken = tokenStore.accessToken()

                val latestHeader =
                    "${NetworkConstants.BEARER} $latestAccessToken"

                if (
                    !latestAccessToken.isNullOrBlank() &&
                    requestToken != latestHeader
                ) {

                    return@runBlocking response.request
                        .newBuilder()
                        .header(
                            NetworkConstants.HEADER_AUTHORIZATION,
                            latestHeader
                        )
                        .build()
                }

                val refreshToken = tokenStore.refreshToken()

                if (refreshToken.isNullOrBlank()) {
                    clearSession()
                    return@runBlocking null
                }

                try {

                    val refreshResponse = authApi.refreshToken(
                        hashMapOf(
                            NetworkConstants.REFRESH_TOKEN to refreshToken
                        )
                    ).execute()

                    if (!refreshResponse.isSuccessful) {
                        clearSession()
                        return@runBlocking null
                    }

                    val body = refreshResponse.body()

                    if (body?.success != 1) {
                        clearSession()
                        return@runBlocking null
                    }

                    val tokens = body.data?.tokens

                    if (tokens == null) {
                        clearSession()
                        return@runBlocking null
                    }

                    saveTokens(tokens)

                    return@runBlocking response.request
                        .newBuilder()
                        .header(
                            NetworkConstants.HEADER_AUTHORIZATION,
                            "${NetworkConstants.BEARER} ${tokens.accessToken}"
                        )
                        .build()

                } catch (e: Exception) {
                    Log.d("TokenAuthenticator", "Exception: $e")
                    clearSession()
                    return@runBlocking null
                }
            }
        }
    }

    private fun saveTokens(tokens: Tokens) {

        runBlocking {
            sessionManager.saveAccessToken(tokens.accessToken)
//            sessionManager.saveRefreshToken(tokens.refreshToken)
        }
        tokenStore.update(
            tokens.accessToken,
//            tokens.refreshToken
        )
    }

    private fun clearSession() {

        runBlocking {
            sessionManager.clearSession()
        }
        tokenStore.clear()
        sessionManager.notifySessionExpired()
    }

    private fun responseCount(response: Response): Int {

        var count = 1
        var priorResponse = response.priorResponse

        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

}