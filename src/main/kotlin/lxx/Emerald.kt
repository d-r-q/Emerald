package lxx

import robocode.AdvancedRobot
import robocode.StatusEvent
import lxx.model.BattleRules
import lxx.model.BattleField
import java.awt.Color
import lxx.events.EventsSource
import lxx.model.BattleStateFactory
import lxx.strategy.FindEnemyStrategy
import lxx.strategy.TurnDecision
import robocode.ScannedRobotEvent
import lxx.strategy.DuelStrategy
import java.util.ArrayList
import lxx.strategy.Strategy
import lxx.strategy.WinStrategy
import lxx.events.FireEvent
import lxx.gun.main.MainGun
import robocode.MouseEvent
import robocode.Event
import lxx.analysis.Collector
import lxx.paint.Canvas
import lxx.paint.LxxGraphics
import java.awt.Graphics2D
import robocode.PaintEvent
import lxx.math.QuickMath
import lxx.movement.WaveSurfingMovement
import lxx.waves.RealWavesWatcher
import lxx.util.Debugger
import lxx.stat.Stat

open class Emerald : AdvancedRobot() {

    {
        QuickMath.init()
    }

    private var allEvents: ArrayList<Event> = arrayListOf()

    override fun onStatus(e: StatusEvent) {
        val rcEvents = getAllEvents()!!
        allEvents = ArrayList(rcEvents)
        allEvents.add(0, e)
        rcEvents.clear()
    }

    override fun onPaint(g: Graphics2D?) {
        // because some magic it's required for PaintEvent appearing in allEvents
    }

    override fun run() {
        if (getOthers() > 1) {
            System.out.println(this.javaClass.getName() + " isn't support battles with more than 1 opponents")
            return
        }

        setColors(Color(0, 2, 1), Color(0x25, 0xFF, 0xC1), Color(0, 0, 0), Color(255, 255, 255), Color(255, 255, 255))
        setAdjustGunForRobotTurn(true)
        setAdjustRadarForGunTurn(true)

        do {
            setTurnRightRadians(java.lang.Double.POSITIVE_INFINITY)
            setTurnGunRightRadians(java.lang.Double.POSITIVE_INFINITY)
            setTurnRadarRightRadians(java.lang.Double.POSITIVE_INFINITY)
            execute()
        } while (allEvents.firstOrNull() { it is ScannedRobotEvent } == null)

        val battleField = BattleField(getBattleFieldWidth(), getBattleFieldHeight(), 18.0)
        val scannedRobotEvent = allEvents.firstOrNull { it is ScannedRobotEvent } as ScannedRobotEvent
        val battleRules = BattleRules(battleField, getWidth(), getHeight(), getGunCoolingRate(), getEnergy(),
                getName()!!, scannedRobotEvent.getName()!!)

        impl(battleRules).run()
    }

    inner class impl(private val battleRules: BattleRules) {

        private val strategies: List<Strategy>
        private val collectors: List<Collector>

        {
            val myWavesWatcher = RealWavesWatcher(battleRules.myName, battleRules.enemyName)

            val debugger = object : Debugger {
                override fun debugProperty(name: String, value: String) {
                    setDebugProperty(name, value)
                }
            }

            val stat = Stat(myWavesWatcher.bulletsStream(), debugger)

            val mainGun = MainGun(battleRules.myName, battleRules.enemyName, myWavesWatcher, stat)
            val waveSurfingMovement = WaveSurfingMovement(battleRules)
            val duelStrategy = DuelStrategy(mainGun, waveSurfingMovement)


            strategies = listOf(FindEnemyStrategy(), duelStrategy, WinStrategy())
            collectors = listOf(mainGun, waveSurfingMovement, stat)
        }

        private val eventsSource = EventsSource<Event>()

        private val battleStateFactory = BattleStateFactory(eventsSource.getEventsStream(lxx.events.allEvents), battleRules, getTime())

        fun run() {

            Canvas.values().forEach { it.reset() }
            val paintEventsSource = eventsSource.getEventsStream {
                it is PaintEvent
            }

            while (true) {
                try {
                    for (event in allEvents.filterNot { it is MouseEvent }) {
                        eventsSource.pushEvent(event)
                    }
                    val newState = battleStateFactory.getNewState()
                    setDebugProperty("Enemy gun heat", newState.enemy.gunHeat.toString())
                    if (!newState.me.alive) {
                        break
                    }

                    for (collector in collectors) {
                        collector.collectData(newState)
                    }

                    if (!newState.enemy.alive && newState.me.heading == 0.0 && newState.me.gunHeading == 0.0 && newState.me.radarHeading == 0.0) {
                        setColors(Color.BLACK, Color.BLACK, Color.BLACK)
                    }

                    val strategy = strategies.firstOrNull() { it.matches(newState) }

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
                } catch (t: Throwable) {
                    System.err.println("Time: " + getTime())
                    t.printStackTrace()
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
            eventsSource.pushEvent(FireEvent(battleRules.myName, bullet))
        }

        private fun aimGun(turnDecision: TurnDecision): Unit {
            setTurnGunRightRadians(turnDecision.gunTurnRate)
        }
    }

}