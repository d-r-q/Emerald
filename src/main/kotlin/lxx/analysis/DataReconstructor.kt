package lxx.analysis

import lxx.model.BattleState

trait DataReconstructor<INPUT, OUTPUT, DATA> {

    public fun destruct(input: INPUT): OUTPUT

    public fun reconstruct(battleState: BattleState, output: OUTPUT, bulletSpeed: Double): DATA

}