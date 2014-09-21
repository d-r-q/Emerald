package lxx.paint

import java.util.ArrayList
import lxx.model.PointLike
import java.awt.Color

enum class Canvas(enabledPropName: String, private val autoReset: Boolean = true) {

    MY_WAVES : Canvas("paint.my.targeting.waves")
    ENEMY_WAVES : Canvas("paint.enemy.targeting.waves")
    MY_TARGETING_PROFILE : Canvas("paint.my.targeting.profile", false)
    MY_MOVEMENT_PROFILE : Canvas("paint.my.movement.profile", false)
    PREDICTED_POSITIONS : Canvas("paint.my.movement.predicted_positions", false)

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

    fun fillCircle(center: PointLike, radius: Double) {
        if (enabled) {
            commands.add { g -> g.fillCircle(center, radius) }
        }
    }

    fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        if (enabled) {
            commands.add { g -> g.drawRect(x, y, width, height) }
        }
    }

    fun drawRect(x: Int, y: Int, width: Int, height: Int) {
        drawRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
    }

    fun fillRect(x: Double, y: Double, width: Double, height: Double, autoReset: Boolean = false) {
        if (enabled) {
            commands.add(object : Function1<LxxGraphics, Unit> {

                var invoked = false

                override fun invoke(g: LxxGraphics) {
                    if (!autoReset || !invoked) {
                        g.fillRect(x, y, width, height)
                    }
                    invoked = true
                }

            })
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