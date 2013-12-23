package lxx.model

data class BattleState(
        val time: Long,
        val me: LxxRobot,
        val opponent: LxxRobot
)