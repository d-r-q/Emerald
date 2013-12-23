package lxx

import robocode.AdvancedRobot
import robocode.StatusEvent
import robocode.BattleResults
import lxx.model.BattleRules
import lxx.model.BattleField
import java.awt.Color
import lxx.events.Log
import lxx.model.BattleStateFactory
import lxx.strategy.FindEnemyStrategy
import lxx.strategy.TurnDecision
import robocode.DeathEvent
import robocode.ScannedRobotEvent
import lxx.strategy.DuelStrategy

open class Neutrino : AdvancedRobot() {

    private var battleRules: BattleRules = BattleRules(BattleField(0.0, 0.0, 0.0, 0.0), 0.0, 0.0, 0.0, 0.0, "")

    private val strategies = listOf(FindEnemyStrategy(), DuelStrategy())

    private val log = Log()

    override fun run() {
        if (getOthers() > 1) {
            System.out.println(getClass().getName() + " isn't support battles with more than 1 opponents")
            return
        }

        setColors(Color(0, 0, 0), Color(255, 255, 150), Color(0, 0, 0), Color(255, 255, 255), Color(255, 255, 255))
        setAdjustGunForRobotTurn(true)
        setAdjustRadarForGunTurn(true)

        val eventsSource = log.getEventsSource { it is ScannedRobotEvent }

        val battleStateFactory = BattleStateFactory(log, getName()!!)

        while (!eventsSource.hasNext()) {
            setTurnRightRadians(java.lang.Double.POSITIVE_INFINITY)
            setTurnGunRightRadians(java.lang.Double.POSITIVE_INFINITY)
            setTurnRadarRightRadians(java.lang.Double.POSITIVE_INFINITY)
            execute()
        }

        while (true) {
            val newState = battleStateFactory.getNewState()
            if (!newState.me.alive) {
                break
            }

            val strategy = strategies.find { it.matches(newState) }

            if (strategy == null) {
                throw AssertionError("Could not find strategy for state $newState")
            }

            val turnDecision = strategy.getTurnDecision(newState)
            move(turnDecision)
            turnRadar(turnDecision)
            handleGun(turnDecision)
            execute()
        }
    }

    private fun move(turnDecision: TurnDecision): Unit {
        setTurnRightRadians(turnDecision.turnRate)
        setAhead(100 * Math.signum(turnDecision.desiredVelocity))
    }

    private fun turnRadar(turnDecision: TurnDecision) {
        setTurnRadarRightRadians(turnDecision.radarTurnRate)
    }

    private fun handleGun(turnDecision: TurnDecision): Unit {
        if (turnDecision.firePower == 0.0 || getGunHeat() > 0 || Math.abs(getGunTurnRemaining()) > 1) {
            aimGun(turnDecision)
            return
        }

        setFireBullet(turnDecision.firePower)

    }
    private fun aimGun(turnDecision: TurnDecision): Unit {
        setTurnGunRightRadians(turnDecision.gunTurnRate)
    }


    override fun onScannedRobot(event: ScannedRobotEvent?) {
        log.pushEvent(event!!)
    }
    override fun onStatus(e: StatusEvent?) {
        if (battleRules.robotWidth == 0.0) {
            val battleField = BattleField(getWidth() / 2, getHeight() / 2, getBattleFieldWidth() - getWidth(), getBattleFieldHeight() - getHeight())
            battleRules = BattleRules(battleField, getWidth(), getHeight(), getGunCoolingRate(), getEnergy(), getName() as String)
        }

        log.pushEvent(e!!.getStatus()!!)
    }


    override fun onDeath(event: DeathEvent?) {
        log.pushEvent(event!!)
    }
}