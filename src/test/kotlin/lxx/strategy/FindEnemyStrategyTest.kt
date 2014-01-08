package lxx.strategy

import org.junit.Test
import lxx.events.Log
import lxx.model.BattleStateFactory
import lxx.RobotStatus
import kotlin.test.assertTrue
import lxx.stdRules

class FindEnemyStrategyTest {

    [Test]
    fun testFindEnemy() {
        val log = Log()
        val battleStateFactory = BattleStateFactory(log, stdRules, 0)
        log.pushEvent(RobotStatus(time = 0))
        val state = battleStateFactory.getNewState()

        val strategy = FindEnemyStrategy()
        assertTrue(strategy.matches(state))
    }

}