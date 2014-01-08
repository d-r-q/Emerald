package lxx.paint

import java.util.ArrayList
import lxx.model.PointLike
import java.awt.Color

enum class Canvas(enabledPropName: String, private val autoReset: Boolean = true) {

    MY_WAVES : Canvas("paint.my.targeting.waves")
    ENEMY_WAVES : Canvas("paint.enemy.targeting.waves")
    MY_TARGETING_PROFILE : Canvas("paint.my.targeting.profile", false)

    var enabled = java.lang.Boolean.getBoolean(enabledPropName)

    val commands = ArrayList<(LxxGraphics) -> Unit>()

    fun setColor(color: Color) {
        if (enabled) {
            commands.add { g -> g.setColor(color) }
        }
    }

    fun drawCircle(center: PointLike, radius: Double) {
        if (enabled) {
            commands.add { g -> g.drawCircle(center, radius) }
        }
    }

    fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        if (enabled) {
            commands.add { g -> g.fillRect(x, y, width, height) }
        }
    }

    fun draw(g: LxxGraphics) {
        for (cmd in commands) {
            cmd(g)
        }
        if (autoReset) {
            reset()
        }
    }

    fun reset() {
        commands.clear()
    }

}