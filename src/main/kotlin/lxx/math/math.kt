package lxx.math

import lxx.math.QuickMath
import java.awt.geom.Point2D

val RADIANS_10 = Math.toRadians(10.0)
val RADIANS_20 = Math.toRadians(20.0)
val RADIANS_90 = Math.toRadians(90.0)

fun angle(baseX: Double, baseY: Double, x: Double, y: Double): Double {
    var theta = QuickMath.asin((y - baseY) / Point2D.distance(x, y, baseX, baseY)) - Math.PI / 2
    if (x >= baseX && theta < 0) {
        theta = -theta
    }

    theta = theta % (Math.PI * 2)
    if ((theta) >= 0) {
        return theta
    }
    else {
        return (theta + Math.PI * 2)
    }
}