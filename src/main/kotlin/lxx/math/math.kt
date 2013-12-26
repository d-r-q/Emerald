package lxx.math

import lxx.math.QuickMath
import java.awt.geom.Point2D
import java.lang.Math.*
import robocode.util.Utils.*

val EPSILON = 0.0001

val RADIANS_0 = Math.toRadians(0.0)
val RADIANS_4 = Math.toRadians(4.0)
val RADIANS_10 = Math.toRadians(10.0)
val RADIANS_20 = Math.toRadians(20.0)
val RADIANS_45 = Math.toRadians(45.0)
val RADIANS_90 = Math.toRadians(90.0)
val RADIANS_100 = Math.toRadians(100.0)
val RADIANS_180 = Math.toRadians(180.0)
val RADIANS_270 = Math.toRadians(270.0)
val RADIANS_360 = Math.toRadians(360.0)

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

fun anglesDiff(alpha1 : Double, alpha2 : Double) = abs(normalRelativeAngle(alpha1 - alpha2))

fun limit(min: Double, x: Double, max: Double) = Math.min(Math.max(x, min), max)