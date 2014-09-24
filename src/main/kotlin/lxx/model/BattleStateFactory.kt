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

class BattleStateFactory(private val eventsSource: Stream<Any>, private val battleRules: BattleRules, val time: Long) {

    private var myPrevState = LxxRobotBuilder().with(newTime = time).build(battleRules)
    private var enemyPrevState = LxxRobotBuilder().with(newTime = time).build(battleRules)
    private var prevBattleState: BattleState? = null

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

                is SkippedTurnEvent -> Logger.warn({ "${event.getSkippedTurn()} tutn skipped" })
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
        assert(enemyPrevState.gunHeat >= 0, "Enemy gun heat = ${enemyPrevState.gunHeat}")
        assert(enemyPrevState.gunHeat <= battleRules.initialGunHeat, "Enemy gun heat = ${enemyPrevState.gunHeat}")

        prevBattleState = BattleState(battleRules, time, myPrevState, enemyPrevState, detectedBullets, prevBattleState)
        return prevBattleState!!
    }
}