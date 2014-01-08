package lxx.model

import robocode.util.Utils
import lxx.paint.Canvas

open data class LxxWave(val time: Long, val attacker: LxxRobot, val victim: LxxRobot, val speed: Double) {

    val zeroBearingOffset = attacker.angleTo(victim)

    fun travelledDistance(time: Long) = (time - this.time) * speed

    fun toBearingOffset(absAngle: Double) = Utils.normalRelativeAngle(absAngle - zeroBearingOffset)

    fun paint(canvas: Canvas, time: Long) {
        canvas.drawCircle(attacker, travelledDistance(time))
    }

}