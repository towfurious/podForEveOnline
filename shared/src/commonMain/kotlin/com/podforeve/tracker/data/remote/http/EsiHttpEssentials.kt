@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.remote.http

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.cache.HttpCache
import kotlin.concurrent.Volatile
import kotlin.time.Clock

// Identifies PodForEve to CCP's ESI ops team per ESI best-practices (so a misbehaving client
// can be reached instead of just banned). Bump the version segment alongside androidApp's
// versionName in androidApp/build.gradle.kts. Contact is an email, not the repo URL — the repo
// is private (confirmed 404 unauthenticated 2026-07-22), so a link to it isn't actually reachable
// by outside parties like CCP's ops team. See ADR-018's 2026-07-22 addendum.
private const val ESI_USER_AGENT = "PodForEve/0.1.0 (viktor.shavarin.dev@gmail.com)"

// Installs the cross-cutting ESI-etiquette concerns shared by both the "sso" and "esi" named
// Ktor clients (see PlatformModule): identify ourselves, respect ESI's own cache headers instead
// of refetching on every call (ESI's docs call unconditional refetching before Expires "cache
// circumvention"), and back off when ESI's per-client error budget gets low.
// See wiki: [[ESI Scopes MVP]].
fun HttpClientConfig<*>.installEsiEssentials() {
    install(UserAgent) { agent = ESI_USER_AGENT }
    install(HttpCache)
    install(EsiRateLimitPlugin)
}

// Thrown when ESI's error-limit budget forces a client-side backoff — either the server just
// responded 420 (Enhance Your Calm), or a prior response already brought the budget low enough
// that we proactively pause instead of spending it down to zero.
class EsiErrorBudgetExhaustedException(message: String) : Exception(message)

// Tracks ESI's per-app error budget from the X-Esi-Error-Limit-Remain / -Reset response headers.
// A shared object (not per-client state) because the budget itself is per source IP / app, not
// per HttpClient instance — the "sso" client's public-ESI calls and the "esi" client's
// authenticated calls draw from the same CCP-side budget.
object EsiErrorBudget {
    private const val LOW_BUDGET_THRESHOLD = 3
    private const val DEFAULT_COOLDOWN_SECONDS = 60L

    @Volatile
    private var cooldownUntilEpochSeconds: Long = 0L

    fun isCoolingDown(nowEpochSeconds: Long): Boolean = nowEpochSeconds < cooldownUntilEpochSeconds

    fun cooldownRemainingSeconds(nowEpochSeconds: Long): Long = (cooldownUntilEpochSeconds - nowEpochSeconds).coerceAtLeast(0)

    // Called after every response that carries the error-limit headers.
    fun observe(remain: Int?, resetSeconds: Int?, nowEpochSeconds: Long) {
        if (remain != null && remain <= LOW_BUDGET_THRESHOLD) {
            cooldownUntilEpochSeconds = nowEpochSeconds + (resetSeconds?.toLong() ?: DEFAULT_COOLDOWN_SECONDS)
        }
    }

    // Called on an actual 420 response, which always means the budget just hit zero.
    fun forceCooldown(resetSeconds: Int?, nowEpochSeconds: Long) {
        cooldownUntilEpochSeconds = nowEpochSeconds + (resetSeconds?.toLong() ?: DEFAULT_COOLDOWN_SECONDS)
    }

    // Test-only: real usage never needs to un-cool-down early.
    internal fun resetForTest() {
        cooldownUntilEpochSeconds = 0L
    }
}

private const val ESI_HOST = "esi.evetech.net"

// Gated by request host: the "sso" client is reused both for public esi.evetech.net calls (in
// scope for CCP's error budget) and for OAuth token exchange/refresh against login.eveonline.com
// (a completely separate service with its own limits). Without this check, an ESI-side 420
// would also block token refresh, turning an unrelated public-endpoint rate limit into a bogus
// "login failed" error.
private val EsiRateLimitPlugin = createClientPlugin("EsiRateLimitPlugin") {
    on(Send) { request ->
        val targetsEsi = request.url.host == ESI_HOST
        if (targetsEsi) {
            val now = Clock.System.now().epochSeconds
            if (EsiErrorBudget.isCoolingDown(now)) {
                throw EsiErrorBudgetExhaustedException(
                    "ESI error budget exhausted — retry in ${EsiErrorBudget.cooldownRemainingSeconds(now)}s",
                )
            }
        }

        val call = proceed(request)

        if (targetsEsi) {
            val now = Clock.System.now().epochSeconds
            val remain = call.response.headers["X-Esi-Error-Limit-Remain"]?.toIntOrNull()
            val reset = call.response.headers["X-Esi-Error-Limit-Reset"]?.toIntOrNull()
            EsiErrorBudget.observe(remain, reset, now)

            if (call.response.status.value == 420) {
                EsiErrorBudget.forceCooldown(reset, now)
                throw EsiErrorBudgetExhaustedException("ESI rate limit hit (420) — retry in ${reset ?: 60}s")
            }
        }
        call
    }
}
