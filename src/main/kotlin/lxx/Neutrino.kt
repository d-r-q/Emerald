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
import java.util.ArrayList
import lxx.strategy.Strategy
import lxx.strategy.WinStrategy
import robocode.BulletHitEvent
import robocode.BulletHitBulletEvent
import robocode.BulletMissedEvent
import robocode.HitByBulletEvent
import robocode.HitRobotEvent
import robocode.HitWallEvent
import robocode.RobotDeathEvent
import robocode.WinEvent
import robocode.RoundEndedEvent
import robocode.BattleEndedEvent
import robocode.SkippedTurnEvent
import java.awt.event.KeyEvent

open class Neutrino : AdvancedRobot() {

    private var battleRules: BattleRules = BattleRules(BattleField(0.0, 0.0, 0.0, 0.0), 0.0, 0.0, 0.0, 0.0, "")

    private val strategies: ArrayList<Strategy> = arrayListOf(FindEnemyStrategy())

    private val log = Log()

    override fun run() {
        if (getOthers() > 1) {
            System.out.println(getClass().getName() + " isn't support battles with more than 1 opponents")
            return
        }

        setColors(Color(2, 1, 0), Color(0xFF, 0xC1, 0x25), Color(0, 0, 0), Color(255, 255, 255), Color(255, 255, 255))
        setAdjustGunForRobotTurn(true)
        setAdjustRadarForGunTurn(true)

        val eventsSource = log.getEventsSource { it is ScannedRobotEvent }

        val battleStateFactory = BattleStateFactory(log, getName()!!, battleRules)

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
            if (!newState.enemy.alive && newState.me.heading == 0.0 && newState.me.gunHeading == 0.0 && newState.me.radarHeading == 0.0) {
                setColors(Color.BLACK, Color.BLACK, Color.BLACK)
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
            strategies.add(DuelStrategy(battleField))
            strategies.add(WinStrategy())
        }

        log.pushEvent(e!!.getStatus()!!)
    }


    override fun onDeath(event: DeathEvent?) {
        log.pushEvent(event!!)
    }


    override fun onBulletHit(event: BulletHitEvent?) {
        log.pushEvent(event!!)
    }
    override fun onBulletHitBullet(event: BulletHitBulletEvent?) {
        log.pushEvent(event!!)
    }
    override fun onBulletMissed(event: BulletMissedEvent?) {
        log.pushEvent(event!!)
    }
    override fun onHitByBullet(event: HitByBulletEvent?) {
        log.pushEvent(event!!)
    }
    override fun onHitRobot(event: HitRobotEvent?) {
        log.pushEvent(event!!)
    }
    override fun onHitWall(event: HitWallEvent?) {
        log.pushEvent(event!!)
    }
    override fun onRobotDeath(event: RobotDeathEvent?) {
        log.pushEvent(event!!)
    }
    override fun onWin(event: WinEvent?) {
        log.pushEvent(event!!)
    }
    override fun onRoundEnded(event: RoundEndedEvent?) {
        log.pushEvent(event!!)
    }
    override fun onBattleEnded(event: BattleEndedEvent?) {
        log.pushEvent(event!!)
    }
    override fun onSkippedTurn(event: SkippedTurnEvent?) {
        log.pushEvent(event!!)
    }
    override fun onKeyTyped(e: KeyEvent?) {
        log.pushEvent(e!!)
    }
}