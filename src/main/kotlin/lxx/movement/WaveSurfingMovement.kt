package lxx.movement

import lxx.analysis.Collector
import lxx.model.BattleState
import lxx.waves.WavesWatcher
import lxx.model.BattleRules
import lxx.model.LxxWave
import robocode.Rules
import lxx.paint.Canvas
import java.awt.Color

public class WaveSurfingMovement(battleRules: BattleRules) : Collector {

    private val wavesWatcher = WavesWatcher(battleRules.enemyName)

    override fun collectData(battleState: BattleState) {
        if (Canvas.ENEMY_WAVES.enabled) {
            Canvas.ENEMY_WAVES.setColor(Color(255, 255, 255, 155))
            wavesWatcher.wavesInAir.forEach {
                it.paint(Canvas.ENEMY_WAVES, battleState.time)
            }
        }

        if (battleState.enemy.firePower == null || battleState.enemy.firePower == 0.0) {
            return
        }

        wavesWatcher.getBrokenWaves(battleState)

        wavesWatcher.watch(LxxWave(battleState.time - 1, battleState.enemy.prevState!!, battleState.me.prevState!!, Rules.getBulletSpeed(battleState.enemy.firePower)))
    }

}