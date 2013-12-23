package lxx.model

import lxx.math.QuickMath
import lxx.math.angle

trait PointLike {

    fun x(): Double

    fun y(): Double

    fun project(alpha: Double, dist: Double) =
            LxxPoint(x() + QuickMath.sin(alpha) * dist, y() + QuickMath.cos(alpha) * dist)

    fun angleTo(another: PointLike) = angle(x(), y(), another.x(), another.y())

}