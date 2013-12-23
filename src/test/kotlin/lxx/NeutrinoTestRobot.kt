package lxx

import java.awt.Color

class NeutrinoTestRobot : Neutrino() {

    var setTurnRadarRightCalled = false

    override fun execute() {
        synchronized (this) {
            wait()
        }
    }

    override fun getBattleFieldWidth() = 800.0

    override fun getBattleFieldHeight() = 600.0

    override fun getWidth() = 32.0

    override fun getHeight() = 32.0

    override fun getGunCoolingRate() = 0.1

    override fun getEnergy() = 100.0

    override fun getName() = ""

    override fun getOthers() = 1


    override fun setColors(bodyColor: Color?, gunColor: Color?, radarColor: Color?, bulletColor: Color?, scanArcColor: Color?) {
    }

    override fun setAdjustGunForRobotTurn(independent: Boolean) {
    }

    override fun setAdjustRadarForGunTurn(independent: Boolean) {
    }

    override fun setTurnRadarRightRadians(degrees: Double) {
        setTurnRadarRightCalled = true
    }
}