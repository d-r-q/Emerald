package lxx.analysis

import lxx.model.BattleState
import java.awt.Color
import lxx.waves.LxxWave
import lxx.paint.Canvas
import lxx.model.LxxRobot
import ags.utils.KdTree
import lxx.waves.WaveWithOffset

class WaveDataCollector<OUTPUT, DATA>(
        locFormula: (LxxRobot, LxxRobot) -> DoubleArray,
        dataReconsturcor: DataReconstructor<WaveWithOffset, OUTPUT, DATA>,
        tree: KdTree<OUTPUT>,
        private val observerName: String,
        private val observableName: String,
        private val waves: Stream<WaveWithOffset>
) : DataCollector<WaveWithOffset, OUTPUT, DATA>(locFormula, dataReconsturcor, tree) {

    override fun collectData(battleState: BattleState) {

        waves.forEach {
            tree.addPoint(getLocation(battleState), dataReconsturcor.destruct(it))
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

}