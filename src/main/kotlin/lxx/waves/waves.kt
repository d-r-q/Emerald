package lxx.waves

import lxx.analysis.Collector
import lxx.events.EventsSource
import lxx.events.allEvents
import lxx.math.RADIANS_180
import lxx.model.BattleState
import lxx.model.LxxPoint
import lxx.model.LxxRobot
import lxx.model.PointLike
import lxx.paint.Canvas
import lxx.rc.prettyString
import lxx.util.Logger
import robocode.Bullet
import robocode.Rules
import robocode.util.Utils
import java.awt.Color
import java.lang.Math.abs
import java.util.*

class WavesWatcher(val attackerName: String,
                   val victimName: String,
                   val detectWave: (BattleState) -> LxxWave?) : Collector {

    var wavesInAir: List<LxxWave> = ArrayList()
        get() = ArrayList(field)

    private var wavesWithoutBullets = HashSet<Wave>()

    private val waveEventsSource = EventsSource<Wave>()

    override fun collectData(battleState: BattleState) {
        val (inAir, broken) = getBrokenWaves(battleState)
        wavesInAir = inAir

        broken.forEach {
            waveEventsSource.pushEvent(it)
            wavesWithoutBullets.remove(it.wave)
        }

        getGoneBullets(battleState).forEach {
            waveEventsSource.pushEvent(it)
        }

        val w = detectWave(battleState)
        if (w != null) {
            watch(w)
        }

        val canvas = if (battleState.me.name == attackerName) Canvas.MY_WAVES else Canvas.ENEMY_WAVES
        wavesInAir.forEach {
            val color = if (wavesWithoutBullets.contains(it)) Color.WHITE else Color(255, 0, 55, 155)
            canvas.setColor(color)
            it.paint(canvas, battleState.time)
        }

    }

    fun brokenWavesStream(): Sequence<BrokenWave> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(BrokenWave::class.java)

    fun hitWavesStream(): Sequence<HitWave> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(HitWave::class.java)

    fun bulletsStream(): Sequence<WaveWithOffset> = waveEventsSource.getEventsStream(allEvents).filterIsInstance(WaveWithOffset::class.java).
            filter {
                it is HitWave || it is BrokenWave && it.hasBullet
            }

    private fun watch(wave: LxxWave) {
        assert(wavesInAir.none { it == wave })
        wavesInAir += wave
    }

    private fun getBrokenWaves(battleState: BattleState): Pair<List<LxxWave>, List<BrokenWave>> {
        val (coming, passed) = wavesInAir.partition { !it.isReached(battleState.robotByName(it.victim.name)) }

        val broken = passed.map {
            BrokenWave(it, it.toBearingOffset(it.attacker.angleTo(battleState.robotByName(it.victim.name))), !wavesWithoutBullets.contains(it))
        }

        return Pair(coming, broken)
    }

    private fun getGoneBullets(battleState: BattleState): List<WaveWithOffset> {
        var hitWaves: List<HitWave> = ArrayList()
        battleState.detectedBullets[attackerName]?.forEach {
            val wave = findWave(battleState.time, it) ?:
                    generateWave(battleState, it)
            if (wave != null) {
                hitWaves += HitWave(wave, wave.toBearingOffset(it.headingRadians), it.victim != null)
                wavesWithoutBullets.add(wave)
            }
        }

        return hitWaves
    }

    private fun findWave(time: Long, bullet: Bullet): LxxWave? {

        val lxxWave = wavesInAir.firstOrNull {

            val travelledDistanceThreshold = when (bullet.victim) {
                null -> it.speed * 2.1
                else -> (it.speed + bullet.velocity) * 1.1
            }

            Utils.isNear(it.speed, bullet.velocity) &&
                    abs(it.travelledDistance(time) - it.attacker.distance(bullet.x, bullet.y)) < travelledDistanceThreshold
        }

        return lxxWave
    }

    private fun generateWave(battleState: BattleState, bullet: Bullet): LxxWave? {
        Logger.warn({ "Generate wave for bullet ${bullet.prettyString()}" })
        val bulletSpeed = bullet.velocity
        val bulletReverseHeading = Utils.normalAbsoluteAngle(bullet.headingRadians + RADIANS_180)
        val currentBulletPos = LxxPoint(bullet.x, bullet.y)

        val pastStates = generateSequence(Pair<BattleState?, LxxPoint>(battleState, currentBulletPos), { Pair(it.first?.prevState, it.second.project(bulletReverseHeading, bulletSpeed)) })
        val state = pastStates.takeWhile {
            val (state, bulletPos) = it
            if (state == null) {
                false
            } else {
                val nextBulletPos = bulletPos.project(bulletReverseHeading, bulletSpeed)
                val attcker = state.robotByName(bullet.name!!)
                bulletPos.distance(attcker) > bulletSpeed &&
                        bulletPos.distance(attcker) > nextBulletPos.distance(attcker)
            }
        }.lastOrNull()

        assert(state != null, { "Could not find wave" })
        return if (state != null && state.first != null) LxxWave(state.first!!, attackerName, victimName, bulletSpeed)
        else null
    }

}

fun RealWavesWatcher(observer: String, observable: String): WavesWatcher {
    val realWavesDetector = { bs: BattleState ->
        val attacker = bs.robotByName(observer)
        if (attacker.firePower != null && attacker.firePower > 0.0) {
            LxxWave(bs.prevState!!, observer, observable, Rules.getBulletSpeed(attacker.firePower))
        } else {
            null
        }
    }

    return WavesWatcher(observer, observable, realWavesDetector)
}

interface Wave : PointLike {

    val time: Long

    val speed: Double

    val center: PointLike

    fun travelledDistance(time: Long) = (time - this.time) * speed

    fun isReached(victim: LxxRobot): Boolean {
        val travelledDistance = travelledDistance(victim.time)
        return travelledDistance > center.distance(victim) && !victim.contains(center.project(center.angleTo(victim), travelledDistance))
    }

    fun flightTime(robot: LxxRobot) = (distance(robot) - travelledDistance(robot.time)) / speed

}

class VirtualWave(override val time: Long,
                  override val center: PointLike,
                  override val speed: Double) : Wave {

    override fun x() = center.x()

    override fun y() = center.y()

}

data class LxxWave(val battleState: BattleState,
                   override val speed: Double,
                   val attacker: LxxRobot,
                   val victim: LxxRobot) : Wave {


    constructor(battleState: BattleState, attackerName: String, victimName: String, speed: Double) :
            this(battleState, speed, battleState.robotByName(attackerName), battleState.robotByName(victimName))

    override val time = battleState.time
    override val center: PointLike = attacker

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

abstract class WaveWithOffset(val wave: LxxWave, val offset: Double) : Wave by wave {

    override fun x() = wave.attacker.x

    override fun y() = wave.attacker.y

}

class BrokenWave(wave: LxxWave, robotOffset: Double, val hasBullet: Boolean) : WaveWithOffset(wave, robotOffset) {

    override fun toString() = "BrokenWave(wave = $wave, offset = $offset)"
}

class HitWave(wave: LxxWave,
              bulletOffset: Double,
              val hitRobot: Boolean) : WaveWithOffset(wave, bulletOffset) {

    override fun toString() = "HitWave(wave = $wave, offset = $offset)"

}
