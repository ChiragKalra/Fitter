package com.bruhascended.fitapp.ui.capturefood

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bruhascended.api.BuildConfig
import com.bruhascended.api.brave.BraveSearchClient
import com.bruhascended.api.gemini.GeminiGroundingClient
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivitySmartCaptureAnalysisBinding
import com.bruhascended.fitapp.ui.addFood.AddCustomFood
import com.bruhascended.fitapp.util.applyStatusBarMarginTop
import com.bruhascended.fitapp.util.setupToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class SmartCaptureAnalysisActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE_PATH = "SMART_CAPTURE_IMAGE_PATH"
        private const val TAG = "SmartCaptureAnalysis"
    }

    private lateinit var binding: ActivitySmartCaptureAnalysisBinding
    private val processor = SmartCaptureProcessor()
    private val modelNotes = mutableListOf<String>()
    private var stageJob: Job? = null
    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_smart_capture_analysis)
        binding.appBarLayout.applyStatusBarMarginTop()
        setupToolbar(binding.toolbar, getString(R.string.smart_capture), home = true)

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        if (imagePath.isNullOrBlank()) {
            Toast.makeText(this, R.string.smart_capture_decode_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imageFile = File(imagePath)
        startAnalysis(imageFile!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun startAnalysis(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        file.delete()

        if (bitmap == null) {
            Toast.makeText(this, R.string.smart_capture_decode_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.capturedImage.setImageBitmap(bitmap)
        pushModelNote(getString(R.string.smart_capture_notes_waiting))
        startStageTicker()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jpegBytes = ByteArrayOutputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    out.toByteArray()
                }

                updateStatus(getString(R.string.smart_capture_stage_grounding))
                val responseText = generateSmartCaptureResponse(jpegBytes)

                updateStatus(getString(R.string.smart_capture_stage_building))
                val draft = processor.parseGeminiResponse(responseText)
                withContext(Dispatchers.Main) {
                    if (draft != null) {
                        launchFoodDraft(draft)
                    } else {
                        Toast.makeText(
                            this@SmartCaptureAnalysisActivity,
                            R.string.smart_capture_no_food,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Smart Capture Gemini request failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SmartCaptureAnalysisActivity,
                        getString(R.string.smart_capture_ai_error, e.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } finally {
                stageJob?.cancel()
            }
        }
    }

    private fun generateSmartCaptureResponse(jpegBytes: ByteArray): String {
        return if (BuildConfig.BRAVE_SEARCH_API_KEY.isNotBlank()) {
            generateBraveGroundedResponse(jpegBytes)
        } else {
            updateStatus(getString(R.string.smart_capture_stage_estimating))
            pushModelNote(getString(R.string.smart_capture_notes_no_brave_key))
            GeminiGroundingClient.streamContentWithoutGoogleSearch(
                apiKey = BuildConfig.GEMINI_API_KEY,
                model = GeminiGroundingClient.DEFAULT_GROUNDING_MODEL,
                textPrompt = finalFoodProfilePrompt(),
                jpegBytes = jpegBytes,
                onThoughtSummary = ::showGeminiThoughtSummary,
                onTextChunk = { updateStatus(getString(R.string.smart_capture_stage_building)) },
            ).text
        }
    }

    private fun generateBraveGroundedResponse(jpegBytes: ByteArray): String {
        updateStatus(getString(R.string.smart_capture_stage_visual_query))
        val searchQuery = GeminiGroundingClient.streamContentWithoutGoogleSearch(
            apiKey = BuildConfig.GEMINI_API_KEY,
            model = GeminiGroundingClient.DEFAULT_GROUNDING_MODEL,
            textPrompt = searchQueryPrompt(),
            jpegBytes = jpegBytes,
            includeThoughts = false,
        ).text.trim().lineSequence().firstOrNull().orEmpty().take(180)

        if (searchQuery.isBlank()) {
            throw IllegalStateException("Gemini returned an empty Brave Search query")
        }

        updateStatus(getString(R.string.smart_capture_stage_brave_search))
        pushModelNote(getString(R.string.smart_capture_notes_brave_query, searchQuery))
        val braveContext = BraveSearchClient.fetchGroundingContext(
            apiKey = BuildConfig.BRAVE_SEARCH_API_KEY,
            query = searchQuery,
        )

        updateStatus(getString(R.string.smart_capture_stage_building))
        pushModelNote(getString(R.string.smart_capture_notes_brave_context))
        return GeminiGroundingClient.streamContentWithoutGoogleSearch(
            apiKey = BuildConfig.GEMINI_API_KEY,
            model = GeminiGroundingClient.DEFAULT_GROUNDING_MODEL,
            textPrompt = finalFoodProfilePrompt(braveContext),
            jpegBytes = jpegBytes,
            onThoughtSummary = ::showGeminiThoughtSummary,
            onTextChunk = { updateStatus(getString(R.string.smart_capture_stage_building)) },
        ).text
    }

    private fun searchQueryPrompt(): String {
        return "Look at this image and create one concise Brave Search query for grounding nutrition facts. " +
            "If it appears to be packaged or restaurant food, include visible brand, product, restaurant, and portion clues. " +
            "If it is generic food, name the food and likely portion. " +
            "Return only the search query, no markdown."
    }

    private fun finalFoodProfilePrompt(braveContext: String? = null): String {
        val context = braveContext
            ?.takeIf { it.isNotBlank() }
            ?.let {
                "Use this Brave Search context as grounding evidence when it is relevant:\n$it\n\n"
            }
            .orEmpty()

        return context +
            "Identify the food in this image and estimate an editable nutrition profile for the visible portion. " +
                "If the grounding context includes packaging, branding, restaurant context, or nutrition labels, prefer it over generic estimates. " +
                "Track added sugar separately from total carbs. Added sugar means sugar added during processing, cooking, sweetening, or restaurant preparation; use 0 when the food appears unsweetened or only has naturally occurring sugars. " +
                "Respond with ONLY valid JSON. Do not wrap it in markdown. " +
                "Use this exact schema: " +
                "{\"food_name\":\"string\",\"quantity\":number,\"quantity_type\":\"Serving|Whole|Slice|Cup|Can|Gram|Milliliter|Kilogram|Liter|Gallon|Tablespoon|Teaspoon|Pound|Pint\",\"calories_kcal\":number,\"carbs_g\":number,\"fat_g\":number,\"protein_g\":number,\"added_sugar_g\":number}. " +
                "The calories, macros, and added sugar must be totals for the estimated quantity, not per 100g."
    }

    private fun startStageTicker() {
        stageJob?.cancel()
        stageJob = lifecycleScope.launch {
            val stages = listOf(
                R.string.smart_capture_stage_prepare,
                R.string.smart_capture_stage_grounding,
                R.string.smart_capture_stage_visual_query,
                R.string.smart_capture_stage_brave_search,
                R.string.smart_capture_stage_building,
            )
            for (stage in stages) {
                delay(2_200L)
                val message = getString(stage)
                updateStatus(message)
                pushModelNote(message)
            }
        }
    }

    private fun updateStatus(status: String) {
        runOnUiThread {
            binding.analysisStatus.text = status
        }
    }

    private fun showGeminiThoughtSummary(summary: String) {
        val trimmed = summary.trim()
        if (trimmed.isEmpty()) return
        pushModelNote(trimmed)
    }

    private fun pushModelNote(note: String) {
        runOnUiThread {
            if (modelNotes.lastOrNull() == note) return@runOnUiThread
            modelNotes.add(note)
            while (modelNotes.size > 4) {
                modelNotes.removeAt(0)
            }
            binding.modelNotes.text = renderMarkdown(modelNotes.joinToString("\n\n"))
            binding.modelNotesScroll.post {
                binding.modelNotesScroll.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
    }

    private fun renderMarkdown(markdown: String): Spanned {
        val html = markdown
            .lineSequence()
            .joinToString("<br>") { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("### ") -> "<b>${TextUtils.htmlEncode(trimmed.removePrefix("### "))}</b>"
                    trimmed.startsWith("## ") -> "<b>${TextUtils.htmlEncode(trimmed.removePrefix("## "))}</b>"
                    trimmed.startsWith("# ") -> "<b>${TextUtils.htmlEncode(trimmed.removePrefix("# "))}</b>"
                    trimmed.startsWith("- ") -> "&#8226; ${TextUtils.htmlEncode(trimmed.removePrefix("- "))}"
                    trimmed.startsWith("* ") -> "&#8226; ${TextUtils.htmlEncode(trimmed.removePrefix("* "))}"
                    else -> TextUtils.htmlEncode(trimmed)
                }
            }
            .replace(Regex("\\*\\*(.+?)\\*\\*")) { "<b>${it.groupValues[1]}</b>" }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    private fun launchFoodDraft(draft: SmartCaptureProcessor.FoodDraft) {
        val intent = Intent(this, AddCustomFood::class.java).apply {
            putExtra(AddCustomFood.EXTRA_DRAFT_FOOD_NAME, draft.foodName)
            putExtra(AddCustomFood.EXTRA_DRAFT_QUANTITY, draft.quantity)
            putExtra(AddCustomFood.EXTRA_DRAFT_QUANTITY_TYPE, draft.quantityType.toString())
            putExtra(AddCustomFood.EXTRA_DRAFT_CALORIES, draft.calories)
            putExtra(AddCustomFood.EXTRA_DRAFT_CARBS, draft.carbs)
            putExtra(AddCustomFood.EXTRA_DRAFT_FAT, draft.fat)
            putExtra(AddCustomFood.EXTRA_DRAFT_PROTEIN, draft.protein)
            putExtra(AddCustomFood.EXTRA_DRAFT_ADDED_SUGAR, draft.addedSugar)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, R.anim.smart_capture_activity_close)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stageJob?.cancel()
        imageFile?.delete()
    }
}
