package lxx.model

data class MaxEscapeAngle(val backward: Double, val forward: Double) {

    val minAngle: Double = Math.min(backward, forward)
    val maxAngle: Double = Math.max(backward, forward)

    fun length() = Math.abs(forward - backward)

}