package lxx.analysis

import lxx.paint.Canvas
import java.awt.Color
import lxx.math.limit

class Profile(
        data: List<Pair<Double, Double>>,
        private val minBearingOffset: Double,
        private val maxBearingOffset: Double,
        private val width: Int = 4
) {

    private val drawWidth = 2.0
    private val drawHeight = 50.0

    private val histogram = DoubleArray(191)
    private val smoothedHistogram = DoubleArray(histogram.size);

    {
        data.forEach {
            val (bo, score) = it
            addScore(bo, score)
        }
    }

    public fun addScore(bearingOffset: Double, score: Double) {
        val idx = toIdx(bearingOffset)
        histogram[idx] += score
        val from = Math.max(0, idx - width)
        val to = Math.min(histogram.size - 1, idx + width)

        for (subIdx in from..to) {
            val dist = Math.abs(idx.toDouble() - subIdx.toDouble()) / width.toDouble()
            smoothedHistogram[subIdx] += 3.0 / 4.0 * (1.0 - dist * dist) * score
        }
    }

    public fun getBestBearingOffset(): Double {
        val zeroOffsetIdx = histogram.size / 2
        [suppress("UNUSED_VARIABLE")]
        val bestIdxPair = smoothedHistogram.withIndices().maxBy {
            val (idx, score) = it
            score
        }

        val bestIdx =
                if (bestIdxPair == null || bestIdxPair.component2() == 0.0) zeroOffsetIdx
                else bestIdxPair.component1()
        return toBearingOffset(bestIdx)
    }

    public fun bearingOffsetDanger(bo: Double): Double = smoothedHistogram[toIdx(bo)]

    private fun toIdx(bearingOffset: Double): Int {

        if (minBearingOffset == maxBearingOffset) {
            return histogram.size / 2;
        }

        val idx = Math.round((bearingOffset - minBearingOffset) / (maxBearingOffset - minBearingOffset) * (histogram.size - 1)).toInt()
        assert(idx >= 0 - 3, "Idx $idx is less than 0, bearingOffset=$bearingOffset, " +
                "data.size=${histogram.size}, minBearingOffset=$minBearingOffset, maxBearingOffset=$maxBearingOffset")
        assert(idx < histogram.size + 3, "Idx $idx is greater than ${histogram.size}, bearingOffset=$bearingOffset, " +
                "data.size=${histogram.size}, minBearingOffset=$minBearingOffset, maxBearingOffset=$maxBearingOffset")

        return limit(0, idx, histogram.size - 1)
    }

    private fun toBearingOffset(idx: Int): Double =
            minBearingOffset + (idx.toDouble() / histogram.size.toDouble() ) * (maxBearingOffset - minBearingOffset)

    public fun drawProfile(canvas: Canvas) {
        if (canvas.enabled) {

            canvas.reset()
            canvas.setColor(Color(255, 255, 255, 155))
            canvas.drawRect(0.0, 0.0, 191 * drawWidth, drawHeight)

            drawData(canvas, Color(0, 255, 0, 150), smoothedHistogram)
            drawData(canvas, Color(0, 0, 255, 150), histogram)
        }
    }

    fun drawCurrentBo(canvas: Canvas, currentBo: Double) {
        val idx = toIdx(currentBo)
        canvas.setColor(Color(255, 255, 0, 100))
        canvas.fillRect(drawWidth * idx, 0.0, drawWidth, drawHeight, true)
    }

    private fun drawData(canvas: Canvas, c: Color, data: DoubleArray) {
        canvas.setColor(c)
        var x = 0.0
        val maxScore = data.max()!!
        for (score in data) {
            canvas.fillRect(x, 0.0, drawWidth, drawHeight * (score / maxScore))
            x += drawWidth
        }
        val idx = toIdx(0.0)
        canvas.setColor(Color(255, 255, 255, 100))
        canvas.fillRect(drawWidth * idx, 0.0, drawWidth, drawHeight)
    }

}