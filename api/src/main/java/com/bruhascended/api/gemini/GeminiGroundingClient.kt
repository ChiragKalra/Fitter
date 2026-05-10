package com.bruhascended.api.gemini

import android.util.Base64
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Calls the [Generative Language API](https://ai.google.dev/api/rest/v1beta/models.generateContent)
 * with **Grounding with Google Search** enabled (`tools: [{ "google_search": {} }]`).
 *
 * The bundled `generativeai` Android artifact does not expose this tool; the REST shape matches
 * the Python `types.Tool(google_search=types.GoogleSearch())` configuration.
 */
object GeminiGroundingClient {

    private const val DEFAULT_MAX_RETRIES = 3
    private const val INITIAL_RETRY_DELAY_MS = 1_000L
    private const val MAX_RETRY_DELAY_MS = 8_000L

    private val jsonType: MediaType? = MediaType.parse("application/json; charset=utf-8")

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Model id for vision + tools; must match [Gemini models](https://ai.google.dev/gemini-api/docs/models). */
    const val DEFAULT_GROUNDING_MODEL = "gemini-3-flash-preview"

    data class GenerateContentResult(
        val text: String,
        val thoughtSummaries: List<String>,
    )

    /**
     * Runs generateContent with an inline JPEG image, text prompt, and Google Search grounding.
     *
     * @throws IOException on HTTP errors, empty/blocked responses, or missing text.
     */
    @Throws(IOException::class)
    fun generateContentWithGoogleSearch(
        apiKey: String,
        model: String = DEFAULT_GROUNDING_MODEL,
        textPrompt: String,
        jpegBytes: ByteArray,
        maxRetries: Int = DEFAULT_MAX_RETRIES,
    ): String {
        return generateContent(
            apiKey = apiKey,
            model = model,
            textPrompt = textPrompt,
            jpegBytes = jpegBytes,
            useGoogleSearch = true,
            maxRetries = maxRetries,
        )
    }

    @Throws(IOException::class)
    fun streamContentWithGoogleSearch(
        apiKey: String,
        model: String = DEFAULT_GROUNDING_MODEL,
        textPrompt: String,
        jpegBytes: ByteArray,
        includeThoughts: Boolean = true,
        onThoughtSummary: (String) -> Unit = {},
        onTextChunk: (String) -> Unit = {},
        maxRetries: Int = DEFAULT_MAX_RETRIES,
    ): GenerateContentResult {
        return streamContent(
            apiKey = apiKey,
            model = model,
            textPrompt = textPrompt,
            jpegBytes = jpegBytes,
            useGoogleSearch = true,
            includeThoughts = includeThoughts,
            onThoughtSummary = onThoughtSummary,
            onTextChunk = onTextChunk,
            maxRetries = maxRetries,
        )
    }

    @Throws(IOException::class)
    fun streamContentWithoutGoogleSearch(
        apiKey: String,
        model: String = DEFAULT_GROUNDING_MODEL,
        textPrompt: String,
        jpegBytes: ByteArray,
        includeThoughts: Boolean = true,
        onThoughtSummary: (String) -> Unit = {},
        onTextChunk: (String) -> Unit = {},
        maxRetries: Int = DEFAULT_MAX_RETRIES,
    ): GenerateContentResult {
        return streamContent(
            apiKey = apiKey,
            model = model,
            textPrompt = textPrompt,
            jpegBytes = jpegBytes,
            useGoogleSearch = false,
            includeThoughts = includeThoughts,
            onThoughtSummary = onThoughtSummary,
            onTextChunk = onTextChunk,
            maxRetries = maxRetries,
        )
    }

    @Throws(IOException::class)
    fun generateContentWithoutGoogleSearch(
        apiKey: String,
        model: String = DEFAULT_GROUNDING_MODEL,
        textPrompt: String,
        jpegBytes: ByteArray,
        maxRetries: Int = DEFAULT_MAX_RETRIES,
    ): String {
        return generateContent(
            apiKey = apiKey,
            model = model,
            textPrompt = textPrompt,
            jpegBytes = jpegBytes,
            useGoogleSearch = false,
            maxRetries = maxRetries,
        )
    }

    private fun generateContent(
        apiKey: String,
        model: String,
        textPrompt: String,
        jpegBytes: ByteArray,
        useGoogleSearch: Boolean,
        maxRetries: Int,
    ): String {
        val root = buildGenerateContentRequest(
            textPrompt = textPrompt,
            jpegBytes = jpegBytes,
            useGoogleSearch = useGoogleSearch,
            includeThoughts = false,
        )

        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val body = RequestBody.create(jsonType, root.toString())
        val request = Request.Builder().url(url).post(body).build()

        var attempt = 0
        while (true) {
            var retryDelayMs: Long? = null
            http.newCall(request).execute().use { response ->
                val respBody = response.body()?.string().orEmpty()
                if (response.isSuccessful) {
                    return extractTextFromGenerateContentJson(respBody)
                }

                val code = response.code()
                if (code == 429 && attempt < maxRetries) {
                    retryDelayMs = retryDelayMillis(response.header("Retry-After"), attempt)
                } else {
                    throw IOException("Gemini HTTP $code: $respBody")
                }
            }
            Thread.sleep(retryDelayMs ?: 0L)
            attempt++
        }
    }

    private fun streamContent(
        apiKey: String,
        model: String,
        textPrompt: String,
        jpegBytes: ByteArray,
        useGoogleSearch: Boolean,
        includeThoughts: Boolean,
        onThoughtSummary: (String) -> Unit,
        onTextChunk: (String) -> Unit,
        maxRetries: Int,
    ): GenerateContentResult {
        val root = buildGenerateContentRequest(
            textPrompt = textPrompt,
            jpegBytes = jpegBytes,
            useGoogleSearch = useGoogleSearch,
            includeThoughts = includeThoughts,
        )

        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val body = RequestBody.create(jsonType, root.toString())
        val request = Request.Builder().url(url).post(body).build()

        var attempt = 0
        while (true) {
            var retryDelayMs: Long? = null
            http.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return readStreamedGenerateContent(
                        responseBody = response.body() ?: throw IOException("Gemini returned no response body"),
                        onThoughtSummary = onThoughtSummary,
                        onTextChunk = onTextChunk,
                    )
                }

                val respBody = response.body()?.string().orEmpty()
                val code = response.code()
                if (code == 429 && attempt < maxRetries) {
                    retryDelayMs = retryDelayMillis(response.header("Retry-After"), attempt)
                } else {
                    throw IOException("Gemini HTTP $code: $respBody")
                }
            }
            Thread.sleep(retryDelayMs ?: 0L)
            attempt++
        }
    }

    private fun buildGenerateContentRequest(
        textPrompt: String,
        jpegBytes: ByteArray,
        useGoogleSearch: Boolean,
        includeThoughts: Boolean,
    ): JSONObject {
        val b64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val parts = JSONArray()
            .put(
                JSONObject().put(
                    "inline_data",
                    JSONObject()
                        .put("mime_type", "image/jpeg")
                        .put("data", b64)
                )
            )
            .put(JSONObject().put("text", textPrompt))

        val root = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", parts)
                )
            )

        if (includeThoughts) {
            root.put(
                "generationConfig",
                JSONObject().put(
                    "thinkingConfig",
                    JSONObject()
                        .put("includeThoughts", true)
                        .put("thinkingLevel", "low")
                )
            )
        }

        if (useGoogleSearch) {
            root.put(
                "tools",
                JSONArray().put(
                    JSONObject().put("google_search", JSONObject())
                )
            )
        }

        return root
    }

    private fun readStreamedGenerateContent(
        responseBody: okhttp3.ResponseBody,
        onThoughtSummary: (String) -> Unit,
        onTextChunk: (String) -> Unit,
    ): GenerateContentResult {
        val text = StringBuilder()
        val thoughts = mutableListOf<String>()
        val source = responseBody.source()
        while (true) {
            val line = source.readUtf8Line() ?: break
            if (!line.startsWith("data:")) continue

            val payload = line.removePrefix("data:").trim()
            if (payload.isEmpty() || payload == "[DONE]") continue

            val event = JSONObject(payload)
            event.optJSONObject("promptFeedback")?.let { fb ->
                val blockReason = fb.optString("blockReason", "")
                if (blockReason.isNotEmpty()) {
                    throw IOException("Prompt blocked: $blockReason")
                }
            }

            forEachTextPart(event) { chunk, isThought ->
                if (isThought) {
                    val summary = chunk.trim()
                    if (summary.isNotEmpty()) {
                        thoughts.add(summary)
                        onThoughtSummary(summary)
                    }
                } else {
                    text.append(chunk)
                    onTextChunk(chunk)
                }
            }
        }

        val finalText = text.toString().trim()
        if (finalText.isEmpty()) {
            throw IOException("Gemini returned no text")
        }
        return GenerateContentResult(finalText, thoughts)
    }

    private fun retryDelayMillis(retryAfter: String?, attempt: Int): Long {
        retryAfter
            ?.trim()
            ?.toLongOrNull()
            ?.takeIf { it > 0L }
            ?.let { return (it * 1_000L).coerceAtMost(MAX_RETRY_DELAY_MS) }

        return (INITIAL_RETRY_DELAY_MS shl attempt).coerceAtMost(MAX_RETRY_DELAY_MS)
    }

    private fun extractTextFromGenerateContentJson(json: String): String {
        val obj = JSONObject(json)
        obj.optJSONObject("promptFeedback")?.let { fb ->
            val blockReason = fb.optString("blockReason", "")
            if (blockReason.isNotEmpty()) {
                throw IOException("Prompt blocked: $blockReason")
            }
        }
        val candidates = obj.optJSONArray("candidates")
            ?: throw IOException("No candidates in Gemini response")
        if (candidates.length() == 0) {
            throw IOException("Empty candidates in Gemini response")
        }
        val first = candidates.getJSONObject(0)
        when (val finish = first.optString("finishReason", "")) {
            "SAFETY", "BLOCKLIST", "RECITATION" ->
                throw IOException("Generation stopped: $finish")
        }
        val content = first.optJSONObject("content")
            ?: throw IOException("No content in Gemini candidate")
        val parts = content.optJSONArray("parts")
            ?: throw IOException("No parts in Gemini content")
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val p = parts.optJSONObject(i) ?: continue
            if (p.optBoolean("thought", false)) continue
            if (p.has("text")) sb.append(p.getString("text"))
        }
        val text = sb.toString().trim()
        if (text.isEmpty()) {
            throw IOException("Gemini returned no text (model may have used only tool calls). Body: ${json.take(500)}")
        }
        return text
    }

    private fun forEachTextPart(
        event: JSONObject,
        onPart: (text: String, isThought: Boolean) -> Unit,
    ) {
        val candidates = event.optJSONArray("candidates") ?: return
        for (candidateIndex in 0 until candidates.length()) {
            val candidate = candidates.optJSONObject(candidateIndex) ?: continue
            val content = candidate.optJSONObject("content") ?: continue
            val parts = content.optJSONArray("parts") ?: continue
            for (partIndex in 0 until parts.length()) {
                val part = parts.optJSONObject(partIndex) ?: continue
                val text = part.optString("text", "")
                if (text.isNotEmpty()) {
                    onPart(text, part.optBoolean("thought", false))
                }
            }
        }
    }
}
