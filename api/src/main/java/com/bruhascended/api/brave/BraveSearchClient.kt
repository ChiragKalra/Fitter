package com.bruhascended.api.brave

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object BraveSearchClient {
    private const val MAX_CONTEXT_CHARS = 8_000

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class)
    fun fetchGroundingContext(
        apiKey: String,
        query: String,
    ): String {
        if (apiKey.isBlank()) {
            throw IOException("Missing Brave Search API key")
        }
        if (query.isBlank()) {
            throw IOException("Missing Brave Search query")
        }

        return fetchWebSearchContext(apiKey, query)
    }

    @Throws(IOException::class)
    private fun fetchWebSearchContext(
        apiKey: String,
        query: String,
    ): String {
        val url = HttpUrl.parse("https://api.search.brave.com/res/v1/web/search")
            ?.newBuilder()
            ?.addQueryParameter("q", query)
            ?.addQueryParameter("count", "8")
            ?.addQueryParameter("country", "us")
            ?.addQueryParameter("search_lang", "en")
            ?.build()
            ?: throw IOException("Invalid Brave Search URL")

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("X-Subscription-Token", apiKey)
            .get()
            .build()

        http.newCall(request).execute().use { response ->
            val body = response.body()?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Brave Web Search HTTP ${response.code()}: $body")
            }
            return summarizeWebSearch(body)
        }
    }

    private fun summarizeWebSearch(json: String): String {
        val root = JSONObject(json)
        val results = root.optJSONObject("web")?.optJSONArray("results") ?: JSONArray()
        val lines = mutableListOf<String>()
        for (i in 0 until results.length()) {
            val result = results.optJSONObject(i) ?: continue
            val title = result.optString("title", "").stripHtml()
            val url = result.optString("url", "")
            val description = result.optString("description", "").stripHtml()
            if (title.isNotBlank() || description.isNotBlank()) {
                lines.add(
                    listOf(title, url, description)
                        .filter { it.isNotBlank() }
                        .joinToString("\n")
                )
            }
        }
        return lines.joinToString("\n\n").take(MAX_CONTEXT_CHARS)
    }

    private fun String.stripHtml(): String {
        return replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
