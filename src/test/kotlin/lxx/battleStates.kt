package lxx

import lxx.model.BattleState
import lxx.events.EventsSource
import robocode.Event
import lxx.model.BattleStateFactory
import lxx.events.robocodeEvents
import robocode.StatusEvent
import lxx.model.LxxRobot

class BattleStates {

    class object {

        fun defaultState() = BattleStates()

    }

    private var time = 0L
    private var me = LxxRobot()
    private var enemy = LxxRobot()

    fun with(time: Long = this.time,
             me: LxxRobot = this.me,
             enemy: LxxRobot = this.enemy): BattleStates {
        this.time = time
        this.me = me
        this.enemy = enemy
        return this
    }

    fun build() = BattleState(stdRules, time, me, enemy, hashMapOf())

}

fun defaultBattleState(): BattleState {
    val eventsSource = EventsSource<Event>()
    val battleStateFactory = BattleStateFactory(eventsSource.getEventsStream(robocodeEvents), stdRules, 0)
    val statusEvent = StatusEvent(RobotStatus(bodyHeading = 0.0, x = 0.0, y = 0.0))
    statusEvent.setTime(3)
    eventsSource.pushEvent(statusEvent)
    eventsSource.pushEvent(ScannedRobotEvent())
    return battleStateFactory.getNewState()
}