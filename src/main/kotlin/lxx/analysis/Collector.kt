package lxx.analysis

import lxx.model.BattleState

interface Collector {

    fun collectData(battleState: BattleState)

}