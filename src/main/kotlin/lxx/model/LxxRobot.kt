package lxx.model

data class LxxRobot(
        val name: String,
        val alive: Boolean,
        val lastScanTime: Long,
        val x: Double,
        val y: Double,
        val heading: Double,
        val gunHeading: Double,
        val radarHeading: Double,
        val gunHeat: Double
        ) : PointLike {

    override fun x() = x
    override fun y() = y

}

fun LxxRobotBuilder(prevState: LxxRobot) = LxxRobotBuilder(
        prevState.name,
        prevState.alive,
        prevState.lastScanTime,
        prevState.x,
        prevState.y,
        prevState.heading,
        prevState.gunHeading,
        prevState.radarHeading,
        prevState.gunHeat
)

data class LxxRobotBuilder(
        var name: String = "Unknown",
        var alive: Boolean = true,
        var lastScanTime: Long = -999,
        var x: Double = java.lang.Double.NaN,
        var y: Double = java.lang.Double.NaN,
        var heading: Double = java.lang.Double.NaN,
        var gunHeading: Double = java.lang.Double.NaN,
        var radarHeading: Double = java.lang.Double.NaN,
        var gunHeat: Double = java.lang.Double.NaN
) : PointLike {

    fun with(newName: String = name,
             newAlive: Boolean = alive,
             newLastScanTime: Long = lastScanTime,
             newX: Double = x,
             newY: Double = y,
             newHeading: Double = heading,
             newGunHeading: Double = gunHeading,
             newRadarHeading: Double = radarHeading,
             newGunHeat: Double = gunHeat) {

        name = newName
        alive = newAlive
        lastScanTime = newLastScanTime
        x = newX
        y = newY
        heading = newHeading
        gunHeading = newGunHeading
        radarHeading = newRadarHeading
        gunHeat = newGunHeat
    }

    fun build() = LxxRobot(name, alive, lastScanTime, x, y, heading, gunHeading, radarHeading, gunHeat)

    override fun x() = x
    override fun y() = y

}