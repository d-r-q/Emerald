package lxx.analysis

import org.junit.Test
import lxx.waves.BrokenWave
import lxx.model.LxxWave
import robocode.Rules
import lxx.model.LxxRobotBuilder
import lxx.stdRules
import lxx.math.*
import lxx.model.*
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class BrokenWaveGFReconstructorTest {

    val dataCollector = BrokenWaveGFReconstructor(stdRules.myName, stdRules.enemyName)

    val attacker = LxxRobotBuilder(x = 0.0, y = 0.0, name = stdRules.myName).build(stdRules)

    [Test]
    fun testReconstruct() {
        val bulletSpeed = Rules.getBulletSpeed(0.1)
        val victim = LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_90, velocity = Rules.MAX_VELOCITY, name = stdRules.enemyName).build(stdRules)
        val battleState = BattleState(stdRules, 0, attacker, victim, mapOf())

        val mea = getMaxEscapeAngle(attacker, victim, bulletSpeed)

        var bo = dataCollector.reconstruct(battleState, 1.0, bulletSpeed)
        assertEquals(mea.forward, bo)

        bo = dataCollector.reconstruct(battleState, 0.5, bulletSpeed)
        assertEquals(0.0, bo)

        bo = dataCollector.reconstruct(battleState, 0.0, bulletSpeed)
        assertEquals(mea.backward, bo)
    }

    [Test]
    fun testDestruct() {
        val bulletSpeed = Rules.getBulletSpeed(0.1)

        testVictim(LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_90, velocity = Rules.MAX_VELOCITY).build(stdRules), bulletSpeed)
        testVictim(LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_90, velocity = -Rules.MAX_VELOCITY).build(stdRules), bulletSpeed)

        testVictim(LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_180, velocity = Rules.MAX_VELOCITY).build(stdRules), bulletSpeed)
        testVictim(LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_180, velocity = -Rules.MAX_VELOCITY).build(stdRules), bulletSpeed)

        testVictim(LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_270, velocity = Rules.MAX_VELOCITY).build(stdRules), bulletSpeed)
        testVictim(LxxRobotBuilder(x = 0.0, y = 300.0, heading = RADIANS_270, velocity = -Rules.MAX_VELOCITY).build(stdRules), bulletSpeed)
    }

    private fun testVictim(victim: LxxRobot, bulletSpeed: Double) {
        val mea = getMaxEscapeAngle(attacker, victim, bulletSpeed)

        var gf = dataCollector.destruct(BrokenWave(LxxWave(0, attacker, victim, bulletSpeed), mea.forward))
        assertTrue(gf == 1.0)

        gf = dataCollector.destruct(BrokenWave(LxxWave(0, attacker, victim, bulletSpeed), 0.0))
        assertTrue(gf == 0.5)

        gf = dataCollector.destruct(BrokenWave(LxxWave(0, attacker, victim, bulletSpeed), mea.backward))
        assertTrue(gf == 0.0)
    }

}