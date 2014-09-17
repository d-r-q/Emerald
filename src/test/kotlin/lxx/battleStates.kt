package lxx

import lxx.model.BattleState
import lxx.events.EventsSource
import robocode.Event
import lxx.model.BattleStateFactory
import lxx.events.robocodeEvents
import robocode.StatusEvent

fun defaultBattleState(): BattleState {
    val eventsSource = EventsSource<Event>()
    val battleStateFactory = BattleStateFactory(eventsSource.getEventsStream(robocodeEvents), stdRules, 0)
    val statusEvent = StatusEvent(RobotStatus(bodyHeading = 0.0, x = 0.0, y = 0.0))
    statusEvent.setTime(3)
    eventsSource.pushEvent(statusEvent)
    eventsSource.pushEvent(ScannedRobotEvent())
    return battleStateFactory.getNewState()
}