package lxx.strategy

import org.junit.Test
import lxx.events.EventsSource
import lxx.events.robocodeEvents
import lxx.model.BattleStateFactory
import lxx.RobotStatus
import kotlin.test.assertTrue
import lxx.stdRules

class FindEnemyStrategyTest {

    [Test]
    fun testFindEnemy() {
        val eventsSource = EventsSource()
        val battleStateFactory = BattleStateFactory(eventsSource.getEventsStream(robocodeEvents), stdRules, 0)
        eventsSource.pushEvent(RobotStatus(time = 0))
        val state = battleStateFactory.getNewState()

        val strategy = FindEnemyStrategy()
        assertTrue(strategy.matches(state))
    }

}