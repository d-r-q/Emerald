package lxx.analysis

import lxx.model.BattleState
import ags.utils.KdTree
import lxx.model.LxxRobot

abstract class DataCollector<INPUT, OUTPUT, DATA>(
        protected val locFormula: (LxxRobot, LxxRobot) -> DoubleArray,
        protected val dataReconsturcor: DataReconstructor<INPUT, OUTPUT, DATA>,
        protected val tree: KdTree<OUTPUT>
) : Collector {

    abstract fun getData(battleState: BattleState, bulletSpeed: Double): List<Pair<DATA, Double>>

}