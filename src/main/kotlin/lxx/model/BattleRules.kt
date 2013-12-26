package lxx.model

data class BattleRules (
        val battleField: BattleField,
        val robotWidth: Double,
        val robotHeight: Double,
        val gunCoolingRate: Double,
        val initialEnergy: Double,
        val myName: String,
        val enemyName: String
) {
    val initialGunHeat = 3
}