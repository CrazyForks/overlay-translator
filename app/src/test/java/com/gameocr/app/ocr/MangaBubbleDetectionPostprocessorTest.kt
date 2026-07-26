package com.gameocr.app.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MangaBubbleDetectionPostprocessorTest {
    @Test
    fun process_tableDriven_filtersClassesConfidenceAndInvalidBoxes() {
        data class Case(
            val name: String,
            val label: Long,
            val score: Float,
            val box: FloatArray,
            val expected: MangaBubbleDetectionPostprocessor.Detection?,
        )
        val cases = listOf(
            Case(
                name = "valid bubble",
                label = 0,
                score = 0.82f,
                box = floatArrayOf(10f, 20f, 90f, 80f),
                expected = MangaBubbleDetectionPostprocessor.Detection(
                    confidence = 0.82f,
                    left = 10f,
                    top = 20f,
                    right = 90f,
                    bottom = 80f,
                ),
            ),
            Case(
                name = "text inside bubble class",
                label = 1,
                score = 0.99f,
                box = floatArrayOf(10f, 20f, 90f, 80f),
                expected = null,
            ),
            Case(
                name = "free text class",
                label = 2,
                score = 0.99f,
                box = floatArrayOf(10f, 20f, 90f, 80f),
                expected = null,
            ),
            Case(
                name = "below confidence",
                label = 0,
                score = 0.29f,
                box = floatArrayOf(10f, 20f, 90f, 80f),
                expected = null,
            ),
            Case(
                name = "coordinates clipped to image",
                label = 0,
                score = 0.71f,
                box = floatArrayOf(-5f, -8f, 130f, 110f),
                expected = MangaBubbleDetectionPostprocessor.Detection(
                    confidence = 0.71f,
                    left = 0f,
                    top = 0f,
                    right = 120f,
                    bottom = 100f,
                ),
            ),
            Case(
                name = "inverted box",
                label = 0,
                score = 0.90f,
                box = floatArrayOf(90f, 80f, 10f, 20f),
                expected = null,
            ),
            Case(
                name = "non finite score",
                label = 0,
                score = Float.NaN,
                box = floatArrayOf(10f, 20f, 90f, 80f),
                expected = null,
            ),
            Case(
                name = "short output row",
                label = 0,
                score = 0.90f,
                box = floatArrayOf(10f, 20f, 90f),
                expected = null,
            ),
        )

        cases.forEach { case ->
            val result = MangaBubbleDetectionPostprocessor.process(
                imageWidth = 120,
                imageHeight = 100,
                labels = longArrayOf(case.label),
                boxes = arrayOf(case.box),
                scores = floatArrayOf(case.score),
            )
            assertEquals(case.name, listOfNotNull(case.expected), result)
        }
    }

    @Test
    fun process_tableDriven_usesShortestOutputAndSortsByConfidence() {
        val result = MangaBubbleDetectionPostprocessor.process(
            imageWidth = 200,
            imageHeight = 160,
            labels = longArrayOf(0, 0, 0),
            boxes = arrayOf(
                floatArrayOf(10f, 10f, 50f, 50f),
                floatArrayOf(60f, 60f, 100f, 100f),
            ),
            scores = floatArrayOf(0.45f, 0.91f, 0.99f),
        )

        assertEquals(2, result.size)
        assertTrue(result[0].confidence > result[1].confidence)
        assertEquals(60f, result[0].left)
    }
}
