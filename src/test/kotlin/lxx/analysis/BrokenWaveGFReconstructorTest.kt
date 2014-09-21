package lxx.analysis

import org.junit.Test
import lxx.waves.BrokenWave
import lxx.waves.LxxWave
import robocode.Rules
import lxx.stdRules
import lxx.math.*
import lxx.model.*
import lxx.BattleStates
import junit.framework.Assert

class BrokenWaveGFReconstructorTest {

    val dataCollector = WaveGfReconstructor(stdRules.myName, stdRules.enemyName)

    val attacker = LxxRobotBuilder(x = 400.0, y = 300.0, name = stdRules.myName).build(stdRules)

    [Test]
    fun testReconstruct() {
        val bulletSpeed = Rules.getBulletSpeed(0.1)
        testVictimReconstruct(bulletSpeed, LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_0, velocity = Rules.MAX_VELOCITY, name = stdRules.enemyName).build(stdRules))
        testVictimReconstruct(bulletSpeed, LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_180, velocity = Rules.MAX_VELOCITY, name = stdRules.enemyName).build(stdRules))
    }

    private fun testVictimReconstruct(bulletSpeed: Double, victim: LxxRobot) {
        val battleState = BattleState(stdRules, 0, attacker, victim, mapOf())

        val mea = preciseMaxEscapeAngle(attacker, victim, bulletSpeed)

        var bo = dataCollector.reconstruct(battleState, 1.0, bulletSpeed)
        Assert.assertEquals(mea.forward, bo)

        bo = dataCollector.reconstruct(battleState, 0.5, bulletSpeed)
        Assert.assertEquals(0.0, bo, 0.1)

        bo = dataCollector.reconstruct(battleState, 0.0, bulletSpeed)
        Assert.assertEquals(mea.backward, bo)
    }

    [Test]
    fun testDestruct() {
        val bulletSpeed = Rules.getBulletSpeed(0.1)

        testVictimDestruct(LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_90, velocity = Rules.MAX_VELOCITY, time = 0).build(stdRules), bulletSpeed)
        testVictimDestruct(LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_90, velocity = -Rules.MAX_VELOCITY, time = 0).build(stdRules), bulletSpeed)

        testVictimDestruct(LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_180, velocity = Rules.MAX_VELOCITY, time = 0).build(stdRules), bulletSpeed)
        testVictimDestruct(LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_180, velocity = -Rules.MAX_VELOCITY, time = 0).build(stdRules), bulletSpeed)

        testVictimDestruct(LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_270, velocity = Rules.MAX_VELOCITY, time = 0).build(stdRules), bulletSpeed)
        testVictimDestruct(LxxRobotBuilder(x = stdRules.robotWidth, y = 300.0, heading = RADIANS_270, velocity = -Rules.MAX_VELOCITY, time = 0).build(stdRules), bulletSpeed)
    }

    private fun testVictimDestruct(victim: LxxRobot, bulletSpeed: Double) {
        val mea = preciseMaxEscapeAngle(attacker, victim, bulletSpeed)
        val bs = BattleStates.defaultState().with(
                time = 3,
                me = attacker,
                enemy = victim).
                build()
        var gf = dataCollector.destruct(BrokenWave(LxxWave(bs, attacker.name, victim.name, bulletSpeed), mea.forward))
        Assert.assertEquals(1.0, gf)

        gf = dataCollector.destruct(BrokenWave(LxxWave(bs, attacker.name, victim.name, bulletSpeed), 0.0))
        Assert.assertEquals(0.5, gf, 0.2)

        gf = dataCollector.destruct(BrokenWave(LxxWave(bs, attacker.name, victim.name, bulletSpeed), mea.backward))
        Assert.assertEquals(0.0, gf)
    }

}