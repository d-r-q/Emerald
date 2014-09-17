package lxx.model

import org.junit.Test
import kotlin.test.assertTrue
import lxx.defaultBattleState

class BattleStateFactoryTest {

    [Test]
    fun testBattleStateFactory() {
        val newState = defaultBattleState()
        assertTrue(newState.me.x == 0.0)
        assertTrue(newState.me.alive)
    }

}