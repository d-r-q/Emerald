package lxx.model

import lxx.math.QuickMath
import lxx.math.*
import java.awt.geom.Point2D

interface PointLike {

    fun x(): Double

    fun y(): Double

    fun project(alpha: Double, dist: Double): LxxPoint {
        assert(alpha >= 0 && alpha <= RADIANS_360, {"Alpha = $alpha"})
        return LxxPoint(x() + QuickMath.sin(alpha) * dist, y() + QuickMath.cos(alpha) * dist)
    }

    fun angleTo(another: PointLike) = angle(x(), y(), another.x(), another.y())

    fun distance(to: PointLike) = Point2D.distance(x(), y(), to.x(), to.y())

    fun distance(toX: Double, toY: Double) = Point2D.distance(x(), y(), toX, toY)

}