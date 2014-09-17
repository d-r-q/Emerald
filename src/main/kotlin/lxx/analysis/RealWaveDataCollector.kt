package lxx.analysis

import lxx.model.BattleState
import lxx.waves.BrokenWave
import java.awt.Color
import lxx.model.LxxWave
import lxx.paint.Canvas
import lxx.waves.WavesWatcher
import robocode.Rules
import lxx.model.LxxRobot
import ags.utils.KdTree
import lxx.analysis.RealWaveDataCollector.CollectionMode

class RealWaveDataCollector<OUTPUT, DATA>(
        locFormula: (LxxRobot, LxxRobot) -> DoubleArray,
        dataReconsturcor: DataReconstructor<BrokenWave, OUTPUT, DATA>,
        tree: KdTree<OUTPUT>,
        private val observerName: String,
        private val observableName: String,
        private val mode: CollectionMode
) : DataCollector<BrokenWave, OUTPUT, DATA>(locFormula, dataReconsturcor, tree) {

    private val wavesWatcher = WavesWatcher(observableName)

    override fun collectData(battleState: BattleState) {
        val brokenWaves = wavesWatcher.getBrokenWaves(battleState)
        if (mode == CollectionMode.VISITS) {
            brokenWaves.forEach { tree.addPoint(getLocation(battleState), dataReconsturcor.destruct(it)) }
        } else if (mode == CollectionMode.HITS) {
            val hitWaves = wavesWatcher.getHitWaves(battleState)
            hitWaves.forEach { tree.addPoint(getLocation(battleState), dataReconsturcor.destruct(it)) }
        }

        val attacker = battleState.robotByName(observerName)
        val victim = battleState.robotByName(observableName)
        assert(attacker.prevState != null)
        assert(victim.prevState != null)

        if (attacker.firePower != null && attacker.firePower > 0.0) {
            val wave = LxxWave(battleState.time - 1, attacker.prevState!!, victim.prevState!!, Rules.getBulletSpeed(attacker.firePower))
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