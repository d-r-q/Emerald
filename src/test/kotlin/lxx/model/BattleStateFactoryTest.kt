package lxx.model

import org.junit.Test
import lxx.defaultBattleState
import org.junit.Assert.assertTrue

class BattleStateFactoryTest {

    @Test
    fun testBattleStateFactory() {
        val newState = defaultBattleState()
        assertTrue(newState.me.x == 0.0)
        assertTrue(newState.me.alive)
    }

}