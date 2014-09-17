package lxx.strategy

import org.junit.Test
import kotlin.test.assertTrue
import lxx.defaultBattleState

class FindEnemyStrategyTest {

    [Test]
    fun testFindEnemy() {
        val strategy = FindEnemyStrategy()
        assertTrue(strategy.matches(defaultBattleState()))
    }

}