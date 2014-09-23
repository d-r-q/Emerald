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
import lxx.util.Logger
import lxx.rc.prettyString
import robocode.Rules

class WavesWatcher(val attackerName: String,
                   val victimName: String,
                   val detectWave: (BattleState) -> LxxWave?) : Collector {

    var wavesInAir: List<LxxWave> = ArrayList()
        get() = ArrayList($wavesInAir)

    private val waveEventsSource = EventsSource<Wave>()

    public override fun collectData(battleState: BattleState) {
        val (inAir, broken) = getBrokenWaves(battleState)
        wavesInAir = inAir

        broken.forEach {
            waveEventsSource.pushEvent(it)
        }

        getHitWaves(battleState).forEach { waveEventsSource.pushEvent(it) }

        val w = detectWave(battleState)
        if (w != null) {
            watch(w)
        }
    }

    public fun brokenWavesStream(): Stream<BrokenWave> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(javaClass<BrokenWave>())

    public fun hitWavesStream(): Stream<HitWave> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(javaClass<HitWave>())

    private fun watch(wave: LxxWave) {
        assert(wavesInAir.none { it == wave })
        wavesInAir += wave
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

    private fun findWave(time: Long, bullet: Bullet): LxxWave? {

        val lxxWave = wavesInAir.firstOrNull() {

            val travelledDistanceThreshold = when (bullet.getVictim()) {
                null -> it.speed * 2.1
                else -> (it.speed + bullet.getVelocity()) * 1.1
            }

            Utils.isNear(it.speed, bullet.getVelocity()) &&
                    abs(it.travelledDistance(time) - it.attacker.distance(bullet.getX(), bullet.getY())) < travelledDistanceThreshold
        }

        return lxxWave
    }

    private fun generateWave(battleState: BattleState, bullet: Bullet): LxxWave? {
        Logger.warn({ "Generate wave for bullet ${bullet.prettyString()}" })
        val bulletSpeed = bullet.getVelocity()
        val bulletReverseHeading = Utils.normalAbsoluteAngle(bullet.getHeadingRadians() + RADIANS_180)
        var currentBulletPos = LxxPoint(bullet.getX(), bullet.getY())

        val pastStates = stream(Pair<BattleState?, LxxPoint>(battleState, currentBulletPos), { Pair(it.first?.prevState, it.second.project(bulletReverseHeading, bulletSpeed)) })
        [suppress("UNUSED_VARIABLE")]
        val state = pastStates.takeWhile {
            val (state, bulletPos) = it
            if (state == null) {
                false
            } else {
                val nextBulletPos = bulletPos.project(bulletReverseHeading, bulletSpeed)
                val attcker = state.robotByName(bullet.getName()!!)
                bulletPos.distance(attcker) > bulletSpeed &&
                        bulletPos.distance(attcker) > nextBulletPos.distance(attcker)
            }
        }.
                lastOrNull()

        assert(state != null, "Could not find wave")
        return if (state != null && state.first != null) LxxWave(state.first!!, attackerName, victimName, bulletSpeed)
        else null
    }

}

fun RealWavesWatcher(observer: String, observable: String): WavesWatcher {
    val realWavesDetector = {(bs: BattleState) ->
        val attacker = bs.robotByName(observer)
        if (attacker.firePower != null && attacker.firePower > 0.0) {
            LxxWave(bs.prevState!!, observer, observable, Rules.getBulletSpeed(attacker.firePower))
        } else {
            null
        }
    }

    return WavesWatcher(observer, observable, realWavesDetector)
}

trait Wave : PointLike {

    val time: Long

    val speed: Double

    val center: PointLike

    fun travelledDistance(time: Long) = (time - this.time) * speed

    fun isReached(victim: LxxRobot): Boolean {
        val travelledDistance = travelledDistance(victim.time)
        return travelledDistance > center.distance(victim) && !victim.contains(center.project(center.angleTo(victim), travelledDistance))
    }

}

class VirtualWave(override val time: Long,
                  override val center: PointLike,
                  override val speed: Double) : Wave {

    override fun x() = center.x()

    override fun y() = center.y()

}

data class LxxWave(val battleState: BattleState,
                   attackerName: String,
                   victimName: String,
                   override val speed: Double) : Wave {

    val attacker = battleState.robotByName(attackerName)
    val victim = battleState.robotByName(victimName)

    override val time = battleState.time
    override val center = attacker

    val zeroBearingOffset = attacker.angleTo(victim)

    fun toBearingOffset(absAngle: Double) = Utils.normalRelativeAngle(absAngle - zeroBearingOffset)

    fun toBearingOffset(pnt: PointLike) = Utils.normalRelativeAngle(attacker.angleTo(pnt) - zeroBearingOffset)

    fun paint(canvas: Canvas, time: Long) {
        canvas.drawCircle(attacker, travelledDistance(time))
    }

    override fun x() = attacker.x

    override fun y() = attacker.y


    override fun toString() = "LxxWave(time = $time, pos = (${attacker.x},${attacker.y}, attacker = ${attacker.name}, victim = ${victim.name}, speed = $speed"
}

abstract class WaveWithOffset(val wave: LxxWave) : Wave by wave {

    abstract val offset: Double

    override fun x() = wave.attacker.x

    override fun y() = wave.attacker.y

}

data class BrokenWave(wave: LxxWave, val hitOffset: Double) : WaveWithOffset(wave) {

    override val offset = hitOffset

    override fun toString() = "BrokenWave(wave = ${wave.toString()}, offset = $hitOffset"
}
data class HitWave(wave: LxxWave, val fireOffset: Double) : WaveWithOffset(wave) {

    override val offset = fireOffset

    override fun toString() = "BrokenWave(wave = ${wave.toString()}, offset = $fireOffset"
}
