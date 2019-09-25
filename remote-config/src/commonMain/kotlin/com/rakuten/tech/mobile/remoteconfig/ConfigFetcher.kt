package com.rakuten.tech.mobile.remoteconfig

//import android.content.Context
//import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
//import okhttp3.Cache
//import okhttp3.HttpUrl
//import okhttp3.OkHttpClient
//import okhttp3.Request
import kotlinx.coroutines.*

internal actual val ApplicationDispatcher: CoroutineDispatcher = Dispatchers.Default

internal class ConfigFetcher constructor(
    baseUrl: String,
    appId: String,
    private val subscriptionKey: String
//    context: Context
) {

//    private val client = OkHttpClient.Builder()
//        .cache(Cache(context.cacheDir, CACHE_SIZE))
//        .addNetworkInterceptor(SdkHeadersInterceptor(appId, context))
//        .build()
    private val client = HttpClient()
    var address = Url("$baseUrl/app/$appId/config")

//    private val requestUrl = try {
//        HttpUrl.get(baseUrl)
//            .newBuilder()
//            .addPathSegments("app/$appId/config")
//            .build()
//    } catch (exception: IllegalArgumentException) {
//        throw InvalidRemoteConfigBaseUrlException(exception)
//    }

    fun fetch(callback: (Map<String, String>) -> Unit) {
        GlobalScope.apply {
            launch(ApplicationDispatcher) {
                val response = client.get<HttpResponse>(address) {
                    header("apiKey", "ras-$subscriptionKey")
                }
                callback(ConfigResponse.fromJsonString(response.readText()).body)
            }
        }
    }

//    private fun buildFetchRequest() = Request.Builder()
//        .url(requestUrl)
//        .addHeader("apiKey", "ras-$subscriptionKey")
//        .build()

    companion object {
        private const val CACHE_SIZE = 1024 * 1024 * 2L
    }
}

@Serializable
private data class ConfigResponse(val body: Map<String, String>) {

    companion object {
        fun fromJsonString(body: String) = Json.nonstrict.parse(serializer(), body)
    }
}

/**
 * Exception thrown when the value set in `AndroidManifest.xml` for
 * `com.rakuten.tech.mobile.remoteconfig.BaseUrl` is not a valid URL.
 */
class InvalidRemoteConfigBaseUrlException(
    exception: IllegalArgumentException
) : IllegalArgumentException("An invalid URL was provided for the Remote Config base url.", exception)
