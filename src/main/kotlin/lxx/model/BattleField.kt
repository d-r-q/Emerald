package lxx.model

import lxx.math.*
import java.lang.Math.max
import robocode.util.Utils

data class BattleField(
        val x: Double,
        val y: Double,
        val width: Double,
        val height: Double
) {

    private val WALL_STICK = 160

    private val availableBottomY = y
    private val availableTopY = y + height
    private val availableLeftX = x
    private val availableRightX = x + width

    private val availableLeftBottom = LxxPoint(availableLeftX, availableBottomY)
    private val availableLeftTop = LxxPoint(availableLeftX, availableTopY)
    private val availableRightTop = LxxPoint(availableRightX, availableTopY)
    private val availableRightBottom = LxxPoint(availableRightX, availableBottomY)


    private val top = Wall(WallType.TOP, RADIANS_0, RADIANS_270, RADIANS_90, availableLeftTop, availableRightTop)
    private val right = Wall(WallType.RIGHT, RADIANS_90, RADIANS_0, RADIANS_180, availableRightTop, availableRightBottom)
    private val bottom = Wall(WallType.BOTTOM, RADIANS_180, RADIANS_90, RADIANS_270, availableRightBottom, availableLeftBottom)
    private val left = Wall(WallType.LEFT, RADIANS_270, RADIANS_180, RADIANS_360, availableLeftBottom, availableLeftTop)

    private val bottomY = 0
    private val topY = y * 2 + height
    private val leftX = 0
    private val rightX = x * 2 + width
    private val leftTop = LxxPoint(leftX.toDouble(), topY.toDouble())
    private val rightTop = LxxPoint(rightX.toDouble(), topY.toDouble())
    private val rightBottom = LxxPoint(rightX.toDouble(), bottomY.toDouble())

    private val noSmoothX = IntervalDouble(WALL_STICK.toDouble(), width - WALL_STICK)
    private val noSmoothY = IntervalDouble(WALL_STICK.toDouble(), height - WALL_STICK)

    // this method is called very often, so keep it optimal
    public fun getWall(pos: PointLike, heading: Double): Wall {
        val normalHeadingTg = QuickMath.tan(heading % RADIANS_90)
        if (heading < RADIANS_90) {
            val rightTopTg = (rightTop.x - pos.x()) / (rightTop.y - pos.y())
            if (normalHeadingTg < rightTopTg) {
                return top
            } else {
                return right
            }
        }
        else if (heading < RADIANS_180) {
            val rightBottomTg = pos.y() / (rightBottom.x - pos.x())
            if (normalHeadingTg < rightBottomTg) {
                return right
            } else {
                return bottom
            }
        } else if (heading < RADIANS_270) {
            val leftBottomTg = pos.x() / pos.y()
            if (normalHeadingTg < leftBottomTg) {
                return bottom
            } else {
                return left
            }
        }
        else if (heading < RADIANS_360) {
            val leftTopTg = (leftTop.y - pos.y()) / pos.x()
            if (normalHeadingTg < leftTopTg) {
                return left
            } else {
                return top
            }
        }

        throw IllegalArgumentException("Invalid heading: " + heading)
    }

    public fun getDistanceToWall(wall: Wall, pnt: PointLike): Double {
        when (wall.wallType) {
            WallType.TOP -> {
                return availableTopY.toDouble() - pnt.y()
            }
            WallType.RIGHT -> {
                return availableRightX.toDouble() - pnt.x()
            }
            WallType.BOTTOM -> {
                return pnt.y() - availableBottomY.toDouble()
            }
            WallType.LEFT -> {
                return pnt.x() - availableLeftX.toDouble()
            }
            else -> {
                throw IllegalArgumentException("Unknown wallType: " + wall.wallType)
            }
        }
    }
    public fun smoothWalls(pnt: PointLike, desiredHeading: Double, isClockwise: Boolean): Double {
        if (noSmoothX.contains(pnt.x()) && noSmoothY.contains(pnt.y())) {
            return desiredHeading
        }

        return smoothWall(getWall(pnt, desiredHeading), pnt, desiredHeading, isClockwise)
    }
    private fun smoothWall(wall: Wall, pnt: PointLike, desiredHeading: Double, isClockwise: Boolean): Double {
        val adjacentLeg = max(0.0, getDistanceToWall(wall, pnt))
        if (WALL_STICK < adjacentLeg)
        {
            return desiredHeading
        }

        val smoothAngle =
                QuickMath.acos(adjacentLeg / WALL_STICK) *
                (if (isClockwise) 1
                else -1).toDouble()

        val baseAngle = wall.fromCenterAngle
        val smoothedAngle = Utils.normalAbsoluteAngle(baseAngle + smoothAngle)
        val secondWall =
                (if (isClockwise) wall.cwWall()
                else wall.ccwWall())
        return smoothWall(secondWall, pnt, smoothedAngle, isClockwise)
    }


    private inner data class Wall(
            val wallType: WallType,
            val fromCenterAngle: Double,
            val counterClockwiseAngle: Double,
            val clockwiseAngle: Double,
            val ccwPoint: LxxPoint,
            val cwPoint: LxxPoint) {

        public fun cwWall(): Wall = when (wallType) {
            WallType.TOP -> right
            WallType.RIGHT -> bottom
            WallType.BOTTOM -> left
            WallType.LEFT -> top
            else -> throw AssertionError("Unknown wall from center angle: ${fromCenterAngle}")
        }

        public fun ccwWall(): Wall = when (wallType) {
            WallType.TOP -> left
            WallType.RIGHT -> top
            WallType.BOTTOM -> right
            WallType.LEFT -> bottom
            else -> throw AssertionError("Unknown wall from center angle: ${fromCenterAngle}")
        }

    }

    private enum class WallType {
        TOP
        RIGHT
        BOTTOM
        LEFT
    }

}