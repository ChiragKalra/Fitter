package com.bruhascended.fitapp.ui.capturefood

import com.bruhascended.db.food.types.QuantityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmartCaptureProcessorTest {

    private val processor = SmartCaptureProcessor()

    @Test
    fun `parseGeminiResponse trims food_name and parses draft`() {
        val json =
            """{"food_name":"  1 cup of rice  ","quantity":1,"quantity_type":"Gram","calories_kcal":180,"carbs_g":40,"fat_g":0.5,"protein_g":4,"added_sugar_g":0}"""
        val draft = processor.parseGeminiResponse(json)
        assertNotNull(draft)
        assertEquals("1 cup of rice", draft!!.foodName)
        assertEquals(180.0, draft.calories, 0.001)
        assertEquals(QuantityType.Gram, draft.quantityType)
    }

    @Test
    fun `parseGeminiResponse returns null when response is null`() {
        assertNull(processor.parseGeminiResponse(null))
    }

    @Test
    fun `parseGeminiResponse returns null for empty trimmed response`() {
        assertNull(processor.parseGeminiResponse(""))
        assertNull(processor.parseGeminiResponse("   "))
    }

    @Test
    fun `parseGeminiResponse unwraps fenced json blob`() {
        val inner =
            """{"food_name":"toast","quantity":2,"quantity_type":"Serving"}"""
        val wrapped = "```json\n$inner\n```"
        val draft = processor.parseGeminiResponse(wrapped)
        assertNotNull(draft)
        assertEquals("toast", draft!!.foodName)
        assertEquals(QuantityType.Serving, draft.quantityType)
    }

    @Test
    fun `parseGeminiResponse returns null when food_name missing`() {
        assertNull(processor.parseGeminiResponse("""{}"""))
    }
}
