package com.gameocr.app.ocr

internal object MangaBubbleDetectionPostprocessor {
    data class Detection(
        val confidence: Float,
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
    )

    fun process(
        imageWidth: Int,
        imageHeight: Int,
        labels: LongArray,
        boxes: Array<FloatArray>,
        scores: FloatArray,
        confidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD,
    ): List<Detection> {
        require(imageWidth > 0 && imageHeight > 0)
        require(confidenceThreshold in 0f..1f)
        val count = minOf(labels.size, boxes.size, scores.size)
        return buildList {
            for (index in 0 until count) {
                if (labels[index] != BUBBLE_LABEL) continue
                val confidence = scores[index]
                val box = boxes[index]
                if (!confidence.isFinite() || confidence < confidenceThreshold || box.size < 4) {
                    continue
                }
                val left = box[0].coerceIn(0f, imageWidth.toFloat())
                val top = box[1].coerceIn(0f, imageHeight.toFloat())
                val right = box[2].coerceIn(0f, imageWidth.toFloat())
                val bottom = box[3].coerceIn(0f, imageHeight.toFloat())
                if (
                    !left.isFinite() ||
                    !top.isFinite() ||
                    !right.isFinite() ||
                    !bottom.isFinite() ||
                    right - left < MIN_BOX_SIDE_PX ||
                    bottom - top < MIN_BOX_SIDE_PX
                ) {
                    continue
                }
                add(
                    Detection(
                        confidence = confidence,
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom,
                    )
                )
            }
        }.sortedByDescending(Detection::confidence)
    }

    const val DEFAULT_CONFIDENCE_THRESHOLD: Float = 0.30f
    private const val BUBBLE_LABEL: Long = 0L
    private const val MIN_BOX_SIDE_PX: Float = 4f
}
