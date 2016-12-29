package lxx.model

import robocode.RobotDeathEvent
import robocode.DeathEvent
import robocode.ScannedRobotEvent

import lxx.events.FireEvent
import robocode.StatusEvent
import java.util.HashMap
import robocode.Bullet
import robocode.BulletHitEvent
import robocode.HitByBulletEvent
import robocode.BulletHitBulletEvent
import java.util.ArrayList
import robocode.util.Utils
import robocode.Rules
import robocode.HitRobotEvent
import lxx.util.Logger
import robocode.SkippedTurnEvent

class BattleStateFactory(private val eventsSource: Sequence<Any>, private val battleRules: BattleRules, val time: Long) {

    private var myPrevState = LxxRobotBuilder().with(newTime = time).build(battleRules)
    private var enemyPrevState = LxxRobotBuilder().with(newTime = time).build(battleRules)
    private var prevBattleState: BattleState? = null

    fun getNewState(): BattleState {

        val detectedBullets = HashMap<String, ArrayList<Bullet>>()
        val myNewState = LxxRobotBuilder(myPrevState)
        val enemyNewState = LxxRobotBuilder(enemyPrevState)
        var time = 0L

        val newEmptyArray = { -> ArrayList<Bullet>() }

        for (event in eventsSource) {
            when (event) {

                is StatusEvent -> {
                    val status = event.status!!
                    myNewState.with(
                            newEnergy = status.energy,
                            newName = battleRules.myName,
                            newTime = event.time,
                            newLastScanTime = event.time,
                            newX = status.x,
                            newY = status.y,
                            newVelocity = status.velocity,
                            newHeading = status.headingRadians,
                            newGunHeading = status.gunHeadingRadians,
                            newRadarHeading = status.radarHeadingRadians,
                            newGunHeat = status.gunHeat
                    )
                    time = event.time
                }

                is ScannedRobotEvent -> {
                    val enemyNewPos = myNewState.project(Utils.normalAbsoluteAngle(myNewState.heading + event.bearingRadians), event.distance)
                    enemyNewState.with(
                            newEnergy = event.energy,
                            newName = event.name!!,
                            newAlive = true,
                            newTime = event.time,
                            newLastScanTime = event.time,
                            newX = enemyNewPos.x,
                            newY = enemyNewPos.y,
                            newVelocity = event.velocity,
                            newHeading = event.headingRadians
                    )
                }

                is FireEvent -> myNewState.with(newFirePower = event.bullet?.power)

                is DeathEvent -> myNewState.with(newAlive = false, newLastScanTime = event.time)

                is RobotDeathEvent -> enemyNewState.with(newAlive = false, newTime = event.time, newLastScanTime = event.time)

                is BulletHitEvent -> {
                    val bullet = event.bullet!!
                    detectedBullets.getOrPut(bullet.name!!, newEmptyArray).add(bullet)

                    val dmg = Rules.getBulletDamage(bullet.power)
                    enemyNewState.takenDamage += dmg
                    myNewState.givenDamage += returnedEnergy(bullet.power)
                }

                is HitByBulletEvent -> {
                    val bullet = event.bullet!!
                    detectedBullets.getOrPut(bullet.name!!, newEmptyArray).add(bullet)

                    val dmg = Rules.getBulletDamage(bullet.power)
                    enemyNewState.givenDamage += returnedEnergy(bullet.power)
                    myNewState.takenDamage += dmg
                }

                is BulletHitBulletEvent -> {
                    val bullet = event.bullet!!
                    detectedBullets.getOrPut(bullet.name!!, newEmptyArray).add(bullet)

                    val hitBullet = event.hitBullet!!
                    detectedBullets.getOrPut(hitBullet.name!!, newEmptyArray).add(hitBullet)
                }

                is HitRobotEvent -> {
                    myNewState.takenDamage += battleRules.hitRobotDamage
                    enemyNewState.takenDamage += battleRules.hitRobotDamage
                }

                is SkippedTurnEvent -> Logger.warn({ "${event.skippedTurn} tutn skipped" })
            }
        }

        assert(time >= 0)

        if (enemyNewState.alive && enemyNewState.time < myNewState.time) {
            Logger.warn({ "Scipped scan: ${myNewState.time}" })
        }

        myPrevState = myNewState.build(battleRules)
        enemyPrevState = enemyNewState.build(battleRules)

        assert(!enemyPrevState.x.isNaN())
        assert(!enemyPrevState.y.isNaN())
        assert(enemyPrevState.gunHeat >= 0, {"Enemy gun heat = ${enemyPrevState.gunHeat}"})
        assert(enemyPrevState.gunHeat <= battleRules.initialGunHeat, {"Enemy gun heat = ${enemyPrevState.gunHeat}"})

        prevBattleState = BattleState(battleRules, time, myPrevState, enemyPrevState, detectedBullets, prevBattleState)
        return prevBattleState!!
    }
}