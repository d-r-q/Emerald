package lxx.analysis

import lxx.model.BattleState
import ags.utils.KdTree
import lxx.events.Log
import lxx.waves.WavesWatcher
import lxx.model.LxxWave
import robocode.Rules
import lxx.model.LxxRobot
import lxx.waves.BrokenWave
import lxx.paint.Canvas
import java.awt.Color

class RealWaveVisitsDataCollector<OUTPUT, DATA>(
        locFormula: (LxxRobot, LxxRobot) -> DoubleArray,
        dataReconsturcor: DataReconstructor<BrokenWave, OUTPUT, DATA>,
        tree: KdTree<OUTPUT>,
        private val observerName: String,
        private val observableName: String
) : DataCollector<BrokenWave, OUTPUT, DATA>(locFormula, dataReconsturcor, tree) {

    private val wavesWatcher = WavesWatcher()


    override fun getData(battleState: BattleState, bulletSpeed: Double): List<Pair<DATA, Double>>  {
        val dataPoints = tree.nearestNeighbor(getLocation(battleState), 100, true)!!
        return dataPoints.map {
            Pair(dataReconsturcor.reconstruct(battleState, it.value!!, bulletSpeed), 1 - (it.distance / dataPoints[0].distance))
        }
    }

    override fun collectData(battleState: BattleState) {
        val brokenWaves = wavesWatcher.getBrokenWaves(battleState)
        brokenWaves.forEach {
            tree.addPoint(getLocation(battleState), dataReconsturcor.destruct(it))
        }

        paintWaves(battleState.time)

        val attacker = battleState.robotByName(observerName)
        val victim = battleState.robotByName(observableName)
        assert(attacker.prevState != null)
        assert(victim.prevState != null)

        if (attacker.firePower != null && attacker.firePower > 0.0) {
            wavesWatcher.watch(LxxWave(battleState.time - 1, attacker.prevState!!, victim.prevState!!, Rules.getBulletSpeed(attacker.firePower)))
        }
    }

    private fun getLocation(battleState: BattleState) =
            locFormula(battleState.robotByName(observerName), battleState.robotByName(observableName))

    private fun paintWaves(time: Long) {

        if (Canvas.MY_WAVES.enabled) {
            Canvas.MY_WAVES.setColor(Color(255, 255, 255, 150))
            for (wave in wavesWatcher.wavesInAir) {
                Canvas.MY_WAVES.drawCircle(wave.attacker, wave.travelledDistance(time))
            }
        }

    }

}