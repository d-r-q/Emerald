package lxx.model

import org.junit.Test
import lxx.events.Log
import lxx.RobotStatus
import lxx.math.*
import kotlin.test.assertTrue

class BattleStateFactoryTest {

    [Test]
    fun testBattleStateFactory() {
        val log = Log()
        val battleStateFactory = BattleStateFactory(log, "")
        log.pushEvent(RobotStatus(x = 0.0, y = 0.0, time = 0L, bodyHeading = RADIANS_90, radarHeading = RADIANS_90))
        val newState = battleStateFactory.getNewState()
        assertTrue(newState.me.x == 0.0)
        assertTrue(newState.me.alive)
    }

}