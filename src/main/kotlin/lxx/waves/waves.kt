package lxx.waves

import java.util.ArrayList
import lxx.model.LxxWave
import lxx.model.BattleState
import robocode.util.Utils

class WavesWatcher {

    var wavesInAir: ArrayList<LxxWave> = ArrayList<LxxWave>()

    public fun watch(wave: LxxWave) {
        wavesInAir.add(wave)
    }

    public fun getBrokenWaves(battleState: BattleState): List<BrokenWave> {
        val (broken, inAir) = wavesInAir.partition {
            it.travelledDistance(battleState.time) >= it.attacker.distance(battleState.robotByName(it.victim.name))
        }

        wavesInAir = inAir as ArrayList<LxxWave>

        return broken.map {
            BrokenWave(it, Utils.normalRelativeAngle(it.attacker.angleTo(battleState.robotByName(it.victim.name)) - it.zeroBearingOffset))
        }
    }

}

data class BrokenWave(val wave: LxxWave, val hitOffset: Double)