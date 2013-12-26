package lxx.analysis

import lxx.paint.Canvas
import java.awt.Color

class Profile(
        private val data: DoubleArray,
        private val minBearingOffset: Double,
        private val maxBearingOffset: Double
) {

    private val smoothedData = DoubleArray(data.size)

    public fun addScore(bearingOffset: Double, score: Double) {
        val idx = toIdx(bearingOffset)
        data[idx] += score
        val width = 4
        val from = Math.max(0, idx - width)
        val to = Math.min(data.size - 1, idx + width)

        for (subIdx in from..to) {
            val dist = Math.abs(idx.toDouble() - subIdx.toDouble()) / width.toDouble()
            smoothedData[subIdx] += 3.0 / 4.0 * (1.0 - dist * dist) * score
        }
    }

    public fun getBestBearingOffset(): Double {
        val zeroOffsetIdx = data.size / 2
        val bestIdxPair = smoothedData.withIndices().maxBy {
            val (idx, score) = it
            score
        }

        val bestIdx =
                if (bestIdxPair == null || bestIdxPair.component2() == 0.0) zeroOffsetIdx
                else bestIdxPair.component1()
        return toBearingOffset(bestIdx)
    }

    private fun toIdx(bearingOffset: Double): Int {

        val idx = Math.floor((bearingOffset - minBearingOffset) / (maxBearingOffset - minBearingOffset) * data.size).toInt()
        assert(idx >= 0, "Idx is less than 0, bearingOffset=$bearingOffset, " +
        "data.size=${data.size}, minBearingOffset=$minBearingOffset, maxBearingOffset=$maxBearingOffset")
        assert(idx < data.size, "Idx is less than 0, bearingOffset=$bearingOffset, " +
        "data.size=${data.size}, minBearingOffset=$minBearingOffset, maxBearingOffset=$maxBearingOffset")
        return idx
    }

    private fun toBearingOffset(idx: Int): Double =
            minBearingOffset + (idx.toDouble() / data.size.toDouble() ) * (maxBearingOffset - minBearingOffset)

    public fun drawProfile(canvas: Canvas) {
        if (canvas.enabled) {
            canvas.reset()
            drawData(canvas, Color(0, 255, 0, 150), smoothedData)
            drawData(canvas, Color(0, 0, 255, 150), data)
        }
    }

    private fun drawData(canvas: Canvas, c: Color, data: DoubleArray) {
        canvas.setColor(c)
        val width = 2.0
        val height = 50.0
        var x = 0.0
        val maxScore = data.max()!!
        for (score in data) {
            canvas.fillRect(x, 0.0, width, height * (score / maxScore))
            x += width
        }
        val idx = toIdx(0.0)
        canvas.setColor(Color(255, 255, 255, 100))
        canvas.fillRect(width * idx, 0.0, width, height)
    }

}