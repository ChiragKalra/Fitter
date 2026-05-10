package com.bruhascended.fitapp.ui.capturefood

import org.junit.Assert.assertEquals
import org.junit.Test

class SmartCaptureProcessorTest {

    private val processor = SmartCaptureProcessor()

    @Test
    fun `parseGeminiResponse should return trimmed string`() {
        val input = "  1 cup of rice  "
        val expected = "1 cup of rice"
        val actual = processor.parseGeminiResponse(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `parseGeminiResponse should return empty string for null`() {
        val actual = processor.parseGeminiResponse(null)
        assertEquals("", actual)
    }

    @Test
    fun `parseGeminiResponse should handle empty response`() {
        val actual = processor.parseGeminiResponse("")
        assertEquals("", actual)
    }
}
