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

class BattleStateFactory(log: Log, private val battleRules: BattleRules) {

    private val filter: (Any) -> Boolean = { it is Event }

    private val eventsSource = log.getEventsSource(filter)

    private var myPrevState = LxxRobotBuilder().build(battleRules)
    private var enemyPrevState = LxxRobotBuilder().build(battleRules)

    fun getNewState(): BattleState {

        var myNewState = LxxRobotBuilder(myPrevState)
        var enemyNewState = LxxRobotBuilder(enemyPrevState)
        var time = 0L

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
                    val enemyNewPos = myNewState.project(myNewState.heading + event.getBearingRadians(), event.getDistance())
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

                is FireEvent -> {
                    myNewState.with(newFirePower = event.bullet?.getPower())
                }

                is DeathEvent -> myNewState.with(newAlive = false, newLastScanTime = event.getTime())

                is RobotDeathEvent -> enemyNewState.with(newAlive = false, newTime = event.getTime(), newLastScanTime = event.getTime())
            }
        }

        assert(time >= 0)

        myPrevState = myNewState.build(battleRules)
        enemyPrevState = enemyNewState.build(battleRules)

        assert(enemyPrevState.x != JDouble.NaN)
        assert(enemyPrevState.y != JDouble.NaN)

        return BattleState(battleRules, time, myPrevState, enemyPrevState)
    }
}