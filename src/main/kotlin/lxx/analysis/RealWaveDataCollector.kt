package lxx.analysis

import lxx.model.BattleState
import java.awt.Color
import lxx.waves.LxxWave
import lxx.paint.Canvas
import lxx.waves.WavesWatcher
import robocode.Rules
import lxx.model.LxxRobot
import ags.utils.KdTree
import lxx.analysis.RealWaveDataCollector.CollectionMode
import lxx.waves.WaveWithOffset

class RealWaveDataCollector<OUTPUT, DATA>(
        locFormula: (LxxRobot, LxxRobot) -> DoubleArray,
        dataReconsturcor: DataReconstructor<WaveWithOffset, OUTPUT, DATA>,
        tree: KdTree<OUTPUT>,
        private val observerName: String,
        private val observableName: String,
        private val mode: CollectionMode
) : DataCollector<WaveWithOffset, OUTPUT, DATA>(locFormula, dataReconsturcor, tree) {

    private val wavesWatcher = WavesWatcher(observerName, observableName)
    private val waves = if (mode == CollectionMode.VISITS) wavesWatcher.brokenWavesStream() else wavesWatcher.hitWavesStream()
    private val hitWaves = wavesWatcher.hitWavesStream()

    override fun collectData(battleState: BattleState) {
        wavesWatcher.collectData(battleState)

        waves.forEach {
            tree.addPoint(getLocation(battleState), dataReconsturcor.destruct(it))
        }

        val attacker = battleState.robotByName(observerName)
        val victim = battleState.robotByName(observableName)
        assert(attacker.prevState != null)
        assert(victim.prevState != null)

        if (attacker.firePower != null && attacker.firePower > 0.0) {
            val wave = LxxWave(battleState.prevState!!, observerName, observableName, Rules.getBulletSpeed(attacker.firePower))
            wavesWatcher.watch(wave)
        }
    }

    protected override fun getLocation(battleState: BattleState) =
            locFormula(battleState.robotByName(observerName), battleState.robotByName(observableName))

    protected fun paintWaves(time: Long, canvas: Canvas, waves: List<LxxWave>) {

        if (canvas.enabled) {
            canvas.setColor(Color(255, 255, 255, 150))
            for (wave in waves) {
                canvas.drawCircle(wave.attacker, wave.travelledDistance(time))
            }
        }

    }

    enum class CollectionMode {
        VISITS
        HITS
    }

}