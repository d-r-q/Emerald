package lxx.model

open data class LxxWave(val time: Long, val attacker: LxxRobot, val victim: LxxRobot, val speed: Double) {

    val zeroBearingOffset = attacker.angleTo(victim)

    fun travelledDistance(time: Long) = (time - this.time) * speed

}