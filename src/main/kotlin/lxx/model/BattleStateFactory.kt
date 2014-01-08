package lxx.model

import robocode.Event
import lxx.events.Log
import robocode.RobotStatus
import robocode.RobotDeathEvent
import robocode.DeathEvent
import robocode.ScannedRobotEvent

import java.lang.Double as JDouble
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

class BattleStateFactory(log: Log, private val battleRules: BattleRules, val time: Long) {

    private val filter: (Any) -> Boolean = { it is Event }

    private val eventsSource = log.getEventsSource(filter)

    private var myPrevState = LxxRobotBuilder().with(newTime = time).build(battleRules)
    private var enemyPrevState = LxxRobotBuilder().with(newTime = time).build(battleRules)

    fun getNewState(): BattleState {

        val detectedBullets = HashMap<String, ArrayList<Bullet>>()
        var myNewState = LxxRobotBuilder(myPrevState)
        var enemyNewState = LxxRobotBuilder(enemyPrevState)
        var time = 0L

        val newEmptyArray = {() -> ArrayList<Bullet>() }

        for (event in eventsSource) {
            when (event) {

                is StatusEvent -> {
                    val status = event.getStatus()!!
                    myNewState.with(
                            newEnergy = status.getEnergy(),
                            newName = battleRules.myName,
                            newTime = event.getTime(),
                            newLastScanTime = event.getTime(),
                            newX = status.getX(),
                            newY = status.getY(),
                            newVelocity = status.getVelocity(),
                            newHeading = status.getHeadingRadians(),
                            newGunHeading = status.getGunHeadingRadians(),
                            newRadarHeading = status.getRadarHeadingRadians(),
                            newGunHeat = status.getGunHeat()
                    )
                    time = event.getTime()
                }

                is ScannedRobotEvent -> {
                    val enemyNewPos = myNewState.project(Utils.normalAbsoluteAngle(myNewState.heading + event.getBearingRadians()), event.getDistance())
                    enemyNewState.with(
                            newEnergy = event.getEnergy(),
                            newName = event.getName()!!,
                            newAlive = true,
                            newTime = event.getTime(),
                            newLastScanTime = event.getTime(),
                            newX = enemyNewPos.x,
                            newY = enemyNewPos.y,
                            newVelocity = event.getVelocity(),
                            newHeading = event.getHeadingRadians()
                    )
                }

                is FireEvent -> myNewState.with(newFirePower = event.bullet?.getPower())

                is DeathEvent -> myNewState.with(newAlive = false, newLastScanTime = event.getTime())

                is RobotDeathEvent -> enemyNewState.with(newAlive = false, newTime = event.getTime(), newLastScanTime = event.getTime())

                is BulletHitEvent -> {
                    val bullet = event.getBullet()!!
                    detectedBullets.getOrPut(bullet.getName()!!, newEmptyArray).add(bullet)

                    val dmg = Rules.getBulletDamage(bullet.getPower())
                    enemyNewState.takenDamage += dmg
                    myNewState.givenDamage += returnedEnergy(bullet.getPower())
                }

                is HitByBulletEvent -> {
                    val bullet = event.getBullet()!!
                    detectedBullets.getOrPut(bullet.getName()!!, newEmptyArray).add(bullet)

                    val dmg = Rules.getBulletDamage(bullet.getPower())
                    enemyNewState.givenDamage += returnedEnergy(bullet.getPower())
                    myNewState.takenDamage += dmg
                }

                is BulletHitBulletEvent -> {
                    val bullet = event.getBullet()!!
                    detectedBullets.getOrPut(bullet.getName()!!, newEmptyArray).add(bullet)

                    val hitBullet = event.getHitBullet()!!
                    detectedBullets.getOrPut(hitBullet.getName()!!, newEmptyArray).add(hitBullet)
                }

                is HitRobotEvent -> {
                    myNewState.takenDamage += battleRules.hitRobotDamage
                    enemyNewState.takenDamage += battleRules.hitRobotDamage
                }
            }
        }

        assert(time >= 0)

        if (enemyNewState.time > enemyPrevState.time + 1) {
            Logger.warn({ "Scipped scans: ${enemyNewState.time - enemyPrevState.time - 1}" })
        }

        myPrevState = myNewState.build(battleRules)
        enemyPrevState = enemyNewState.build(battleRules)

        assert(enemyPrevState.x != JDouble.NaN)
        assert(enemyPrevState.y != JDouble.NaN)
        assert(enemyPrevState.gunHeat >= 0, "Enemy gun heat = ${enemyPrevState.gunHeat}")
        assert(enemyPrevState.gunHeat <= battleRules.initialGunHeat, "Enemy gun heat = ${enemyPrevState.gunHeat}")

        return BattleState(battleRules, time, myPrevState, enemyPrevState, detectedBullets)
    }
}