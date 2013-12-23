package lxx.model

data class LxxPoint(val x: Double, val y: Double) : PointLike {

    override fun x() = x
    override fun y() = y

}