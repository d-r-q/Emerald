package lxx.model

import lxx.math.QuickMath
import lxx.math.angle
import java.awt.geom.Point2D

trait PointLike {

    fun x(): Double

    fun y(): Double

    fun project(alpha: Double, dist: Double) =
            LxxPoint(x() + QuickMath.sin(alpha) * dist, y() + QuickMath.cos(alpha) * dist)

    fun angleTo(another: PointLike) = angle(x(), y(), another.x(), another.y())

    fun distance(to: PointLike) = Point2D.distance(x(), y(), to.x(), to.y())

}