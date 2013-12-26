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
import lxx.events.FireEvent
import lxx.gun.main.MainGun
import robocode.MouseEvent
import robocode.Event
import lxx.analysis.Collector
import lxx.paint.Canvas
import lxx.paint.LxxGraphics
import java.awt.Graphics2D
import robocode.PaintEvent

open class Neutrino : AdvancedRobot() {

    private var allEvents: ArrayList<Event> = arrayListOf()

    override fun onStatus(e: StatusEvent?) {
        val rcEvents = getAllEvents()!!
        allEvents = ArrayList(rcEvents)
        allEvents.add(0, e!!)
        rcEvents.clear()
    }

    override fun onPaint(g: Graphics2D?) {
        // because some magic it's required for PaintEvent appearing in allEvents
    }

    override fun run() {
        if (getOthers() > 1) {
            System.out.println(getClass().getName() + " isn't support battles with more than 1 opponents")
            return
        }

        setColors(Color(2, 1, 0), Color(0xFF, 0xC1, 0x25), Color(0, 0, 0), Color(255, 255, 255), Color(255, 255, 255))
        setAdjustGunForRobotTurn(true)
        setAdjustRadarForGunTurn(true)

        do {
            setTurnRightRadians(java.lang.Double.POSITIVE_INFINITY)
            setTurnGunRightRadians(java.lang.Double.POSITIVE_INFINITY)
            setTurnRadarRightRadians(java.lang.Double.POSITIVE_INFINITY)
            execute()
        } while (allEvents.find { it is ScannedRobotEvent } == null)

        val battleField = BattleField(getWidth() / 2, getHeight() / 2, getBattleFieldWidth() - getWidth(), getBattleFieldHeight() - getHeight())
        val scannedRobotEvent = allEvents.find { it is ScannedRobotEvent } as ScannedRobotEvent
        val battleRules = BattleRules(battleField, getWidth(), getHeight(), getGunCoolingRate(), getEnergy(),
                getName()!!, scannedRobotEvent.getName()!!)

        impl(battleRules).run()
    }

    inner class impl(private val battleRules: BattleRules) {

        private val strategies: List<Strategy>
        private val collectors: List<Collector>

        {
            val mainGun = MainGun(battleRules.myName, battleRules.enemyName)
            val duelStrategy = DuelStrategy(battleRules.battleField, mainGun)
            strategies = listOf(FindEnemyStrategy(), duelStrategy, WinStrategy())
            collectors = listOf(mainGun)
        }

        private val log = Log()

        private val battleStateFactory = BattleStateFactory(log, battleRules)

        fun run() {

            val paintEventsSource = log.getEventsSource {
                it is PaintEvent
            }

            while (true) {
                for (event in allEvents.filterNot { it is MouseEvent }) {
                    log.pushEvent(event)
                }
                val newState = battleStateFactory.getNewState()
                if (!newState.me.alive) {
                    break
                }

                for (collector in collectors) {
                    collector.collectData(newState)
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

                paintEventsSource.any { g ->
                    val graphics = LxxGraphics(getGraphics()!!)
                    Canvas.values().forEach { it.draw(graphics) }
                    true
                }

                execute()
            }
        }

        private fun move(turnDecision: TurnDecision): Unit {
            setTurnRightRadians(turnDecision.turnRate)
            setAhead(turnDecision.desiredVelocity)
        }

        private fun turnRadar(turnDecision: TurnDecision) {
            setTurnRadarRightRadians(turnDecision.radarTurnRate)
        }

        private fun handleGun(turnDecision: TurnDecision): Unit {
            if (turnDecision.firePower == 0.0 || getGunHeat() > 0 || Math.abs(getGunTurnRemaining()) > 1) {
                aimGun(turnDecision)
                return
            }

            val bullet = setFireBullet(turnDecision.firePower)
            log.pushEvent(FireEvent(battleRules.myName, bullet))
        }

        private fun aimGun(turnDecision: TurnDecision): Unit {
            setTurnGunRightRadians(turnDecision.gunTurnRate)
        }
    }

}