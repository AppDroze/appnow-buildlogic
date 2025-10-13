package com.appnow.samples

expect fun platformName(): String

class Greeting {
    fun greeting(): String = "Hello from " + platformName()
}

