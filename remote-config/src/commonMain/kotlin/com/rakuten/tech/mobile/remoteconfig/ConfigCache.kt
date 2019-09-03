package com.rakuten.tech.mobile.remoteconfig

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Suppress("TooGenericExceptionCaught")
internal class ConfigCache constructor(
        fetcher: ConfigFetcher,
        poller: AsyncPoller
) {

    constructor(fetcher: ConfigFetcher) : this(
        fetcher,
        AsyncPoller(DELAY_IN_MINUTES)
    )

    private val config = Config(emptyMap())

    init {
        poller.start {
            try {
                val fetchedConfig = fetcher.fetch()
                val configJson = Config(fetchedConfig).toJsonString()
                print(configJson)

//                file.writeText(configJson)
            } catch (error: Exception) {
//                Log.e("RemoteConfig", "Error while fetching config from server", error)
            }
        }
    }

    operator fun get(key: String) = config.values[key]

    fun getConfig(): Map<String, String> = config.values

    companion object {
        const val DELAY_IN_MINUTES: Int = 60
    }
}

@Serializable
private data class Config(val values: Map<String, String>) {

    fun toJsonString() = Json.stringify(serializer(), this)

    companion object {
        fun fromJsonString(body: String) = Json.nonstrict.parse(serializer(), body)
    }
}
