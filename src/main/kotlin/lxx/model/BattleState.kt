package lxx.model

import robocode.Bullet

data class BattleState(
        val rules: BattleRules,
        val time: Long,
        val me: LxxRobot,
        val enemy: LxxRobot,
        val detectedBullets: Map<String, List<Bullet>>
) {

    val battleField = rules.battleField

    fun robotByName(name: String) =
            if (name.equals(me.name)) me
            else enemy

}