package lxx.math

class IntervalDouble(val a: Double, val b: Double) {

    fun contains(x: Double) = a <= x && x <= b

}