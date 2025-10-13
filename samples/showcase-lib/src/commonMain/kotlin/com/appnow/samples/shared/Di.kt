package com.appnow.samples.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin

// Simple service
class Greeter(
    private val platformName: String,
    private val now: () -> String
) {
    fun greet(): String = "Hello from $platformName @ ${now()}"
}

private val appModule = module {
    single(named("platformName")) { Platform.platformName() }
    single(named("timeProvider")) {
        {
            val instant = Clock.System.now()
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${local.date} ${local.hour}:${local.minute}:${local.second}"
        }
    }
    single { Greeter(get(named("platformName")), get(named("timeProvider"))) }
}

/** Idempotent init (safe to call multiple times on both platforms). */
fun initKoin() {
    if (runCatching { getKoin() }.isFailure) {
        startKoin { modules(appModule) }
    }
}

/** Public API used by Android & iOS UIs. */
fun helloFromKoin(): String {
    val koin = getKoin()
    val greeter: Greeter = koin.get()
    return greeter.greet()
}

