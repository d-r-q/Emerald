package lxx.model

data class BattleState(
        val rules: BattleRules,
        val time: Long,
        val me: LxxRobot,
        val enemy: LxxRobot
)