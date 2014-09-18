package lxx.waves

import java.util.ArrayList
import lxx.model.BattleState
import robocode.util.Utils
import robocode.Bullet
import java.lang.Math.abs
import lxx.model.LxxPoint
import lxx.math.*
import lxx.analysis.Collector
import lxx.events.EventsSource
import lxx.events.allEvents
import lxx.model.LxxRobot
import lxx.paint.Canvas
import lxx.model.PointLike

class WavesWatcher(val attackerName: String,
                   val victimName: String) : Collector {

    var wavesInAir: List<LxxWave> = ArrayList()

    private val waveEventsSource = EventsSource<Wave>()

    public fun watch(wave: LxxWave) {
        wavesInAir += wave
    }

    public fun brokenWavesStream(): Stream<BrokenWave> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(javaClass<BrokenWave>())

    public fun hitWavesStream(): Stream<HitWave> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(javaClass<HitWave>())

    public override fun collectData(battleState: BattleState) {
        val (inAir, broken) = getBrokenWaves(battleState)
        wavesInAir = inAir

        broken.forEach {
            waveEventsSource.pushEvent(it)
        }

        getHitWaves(battleState).forEach { waveEventsSource.pushEvent(it) }
    }

    private fun getBrokenWaves(battleState: BattleState): Pair<List<LxxWave>, List<BrokenWave>> {
        val (coming, passed) = wavesInAir.partition { !it.isReached(battleState.robotByName(it.victim.name)) }

        val broken = passed.map {
            BrokenWave(it, it.toBearingOffset(it.attacker.angleTo(battleState.robotByName(it.victim.name))))
        }

        return Pair(coming, broken)
    }

    private fun getHitWaves(battleState: BattleState): List<HitWave> {
        var hitWaves: List<HitWave> = ArrayList()
        battleState.detectedBullets[attackerName]?.forEach {
            val wave = findWave(battleState.time, it) ?:
                    generateWave(battleState, it)
            if (wave != null) {
                hitWaves += HitWave(wave, wave.toBearingOffset(it.getHeadingRadians()))
            }
        }

        return hitWaves
    }

    private fun findWave(time: Long, bullet: Bullet) = wavesInAir.firstOrNull() {
        Utils.isNear(it.speed, bullet.getVelocity()) &&
                abs(it.travelledDistance(time) - it.attacker.distance(bullet.getX(), bullet.getY())) < bullet.getVelocity()
    }

    private fun generateWave(battleState: BattleState, bullet: Bullet): LxxWave? {
        val bulletSpeed = bullet.getVelocity()
        val bulletReverseHeading = Utils.normalAbsoluteAngle(bullet.getHeadingRadians() + RADIANS_180)
        var currentBulletPos = LxxPoint(bullet.getX(), bullet.getY())

        val pastStates = stream(Pair(battleState, currentBulletPos), { Pair(it.first.prevState!!, it.second.project(bulletReverseHeading, bulletSpeed)) })
        [suppress("UNUSED_VARIABLE")]
        val state = pastStates.takeWhile {
            val (state, bulletPos) = it
            if (state.prevState == null) {
                false
            } else {
                val nextBulletPos = bulletPos.project(bulletReverseHeading, bulletSpeed)
                val attcker = state.robotByName(bullet.getName()!!)
                bulletPos.distance(nextBulletPos) <= bulletPos.distance(attcker) &&
                        bulletPos.distance(attcker) > nextBulletPos.distance(attcker)
            }
        }.
                lastOrNull()

        assert(state != null, "Could not find wave")
        return if (state != null) LxxWave(state.first, attackerName, victimName, bulletSpeed)
        else null
    }

}

abstract class Wave

data class LxxWave(val battleState: BattleState, attackerName: String, victimName: String, val speed: Double) : Wave() {

    val time = battleState.time
    val attacker = battleState.robotByName(attackerName)
    val victim = battleState.robotByName(victimName)

    val zeroBearingOffset = attacker.angleTo(victim)

    fun travelledDistance(time: Long) = (time - this.time) * speed

    fun toBearingOffset(absAngle: Double) = Utils.normalRelativeAngle(absAngle - zeroBearingOffset)

    fun toBearingOffset(pnt: PointLike) = Utils.normalRelativeAngle(attacker.angleTo(pnt) - zeroBearingOffset)

    fun paint(canvas: Canvas, time: Long) {
        canvas.drawCircle(attacker, travelledDistance(time))
    }

    fun isReached(victim: LxxRobot): Boolean = travelledDistance(victim.time) > attacker.distance(victim)

}

abstract class WaveWithOffset : Wave() {

    abstract val wave: LxxWave

    abstract val offset: Double

}

data class BrokenWave(override val wave: LxxWave, val hitOffset: Double) : WaveWithOffset() {
    override val offset = hitOffset
}

data class HitWave(override val wave: LxxWave, val fireOffset: Double) : WaveWithOffset() {
    override val offset = fireOffset
}
