package lxx.strategy

data class TurnDecision(
        val desiredVelocity: Double,
        val turnRate: Double,

        val gunTurnRate: Double,
        val firePower: Double,

        val radarTurnRate: Double
)