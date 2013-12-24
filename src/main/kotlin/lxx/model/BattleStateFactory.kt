package lxx.model

import robocode.Event
import lxx.events.Log
import robocode.RobotStatus
import robocode.RobotDeathEvent
import robocode.DeathEvent
import robocode.ScannedRobotEvent

import java.lang.Double as JDouble

class BattleStateFactory(log: Log, private val myName: String, private val rules: BattleRules) {

    private val filter: (Any) -> Boolean = {
        it is Event || it is RobotStatus
    }

    private val eventsSource = log.getEventsSource(filter)

    private var myPrevState = LxxRobotBuilder().build()
    private var enemyPrevState = LxxRobotBuilder().build()

    fun getNewState(): BattleState {

        var myNewState = LxxRobotBuilder(myPrevState)
        var enemyNewState = LxxRobotBuilder(enemyPrevState)
        var time = 0L

        for (event in eventsSource) {
            when (event) {

                is RobotStatus -> {
                    myNewState.with(
                            newName = myName,
                            newLastScanTime = event.getTime(),
                            newX = event.getX(),
                            newY = event.getY(),
                            newHeading = event.getHeadingRadians(),
                            newGunHeading = event.getGunHeadingRadians(),
                            newRadarHeading = event.getRadarHeadingRadians(),
                            newGunHeat = event.getGunHeat()
                    )
                    time = event.getTime()
                }

                is ScannedRobotEvent -> {
                    val enemyNewPos = myNewState.project(myNewState.heading + event.getBearingRadians(), event.getDistance())
                    enemyNewState.with(
                            newName = event.getName()!!,
                            newAlive = true,
                            newLastScanTime = event.getTime(),
                            newX = enemyNewPos.x,
                            newY = enemyNewPos.y,
                            newHeading = event.getHeadingRadians()
                    )
                }

                is DeathEvent -> {
                    myNewState.with(newAlive = false, newLastScanTime = event.getTime())
                    time = event.getTime()
                }

                is RobotDeathEvent -> {
                    enemyNewState.with(newAlive = false, newLastScanTime = event.getTime())
                    time = event.getTime()
                }
            }
        }

        assert(time >= 0)

        myPrevState = myNewState.build()
        enemyPrevState = enemyNewState.build()

        assert(enemyPrevState.x != JDouble.NaN)
        assert(enemyPrevState.y != JDouble.NaN)

        return BattleState(rules, time, myPrevState, enemyPrevState)
    }
}