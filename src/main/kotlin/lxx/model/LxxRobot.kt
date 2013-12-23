package lxx.model

data class LxxRobot(
        val name: String,
        val alive: Boolean,
        val lastScanTime: Long,
        val x: Double,
        val y: Double,
        val heading: Double,
        val gunHeading: Double,
        val radarHeading: Double
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
        prevState.radarHeading
)

data class LxxRobotBuilder(
        var name: String = "Unknown",
        var alive: Boolean = true,
        var lastScanTime: Long = -999,
        var x: Double = java.lang.Double.NaN,
        var y: Double = java.lang.Double.NaN,
        var heading: Double = java.lang.Double.NaN,
        var gunHeading: Double = java.lang.Double.NaN,
        var radarHeading: Double = java.lang.Double.NaN
) : PointLike {

    fun with(newName: String = name,
             newAlive: Boolean = alive,
             newLastScanTime: Long = lastScanTime,
             newX: Double = x,
             newY: Double = y,
             newHeading: Double = heading,
             newGunHeading: Double = gunHeading,
             newRadarHeading: Double = radarHeading) {

        name = newName
        alive = newAlive
        lastScanTime = newLastScanTime
        x = newX
        y = newY
        heading = newHeading
        gunHeading = newGunHeading
        radarHeading = newRadarHeading
    }

    fun build() = LxxRobot(name, alive, lastScanTime, x, y, heading, gunHeading, radarHeading)

    override fun x() = x
    override fun y() = y

}