package lxx.model

data class BattleState(
        val rules: BattleRules,
        val time: Long,
        val me: LxxRobot,
        val enemy: LxxRobot
) {

    val battleField = rules.battleField

    fun robotByName(name: String) =
            if (name.equals(me.name)) me
            else enemy

}