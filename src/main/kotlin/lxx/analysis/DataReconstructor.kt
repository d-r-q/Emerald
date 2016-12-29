package lxx.analysis

import lxx.model.BattleState

interface DataReconstructor<INPUT, OUTPUT, DATA> {

    fun destruct(input: INPUT): OUTPUT

    fun reconstruct(battleState: BattleState, output: OUTPUT, bulletSpeed: Double): DATA

}