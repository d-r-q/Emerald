package lxx.strategy

import org.junit.Test
import lxx.defaultBattleState
import org.junit.Assert.assertTrue

class FindEnemyStrategyTest {

    @Test
    fun testFindEnemy() {
        val strategy = FindEnemyStrategy()
        assertTrue(strategy.matches(defaultBattleState()))
    }

}