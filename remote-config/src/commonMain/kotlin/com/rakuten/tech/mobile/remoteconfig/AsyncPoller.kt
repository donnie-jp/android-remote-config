package com.rakuten.tech.mobile.remoteconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class AsyncPoller constructor(
    delayInMinutes: Int,
    private val scope: CoroutineScope
) {

    constructor(delayInMinutes: Int) : this(delayInMinutes, GlobalScope)

    private val delayInMilliseconds = delayInMinutes.toLong() * 60 * 1000

    fun start(method: () -> Any) {
        scope.launch {
            repeat(Int.MAX_VALUE) {
                method.invoke()

                delay(delayInMilliseconds)
            }
        }
    }
}
