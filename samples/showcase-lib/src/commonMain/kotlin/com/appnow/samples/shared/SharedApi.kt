package com.appnow.samples.shared

/**
 * Public API for both Android and iOS.
 * In Swift, access as: SharedApi.shared.initialize() and SharedApi.shared.getMessage()
 */
object SharedApi {
    /** Initialize Koin DI (safe to call multiple times) */
    fun initialize() {
        initKoin()
    }
    
    /** Get greeting message with current time */
    fun getMessage(): String {
        return helloFromKoin()
    }
}

