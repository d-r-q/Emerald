package lxx.paint

import java.awt.Graphics2D
import java.awt.Color
import lxx.model.PointLike
import java.lang.Math.*

class LxxGraphics(private val g: Graphics2D) {

    fun drawLine(from: PointLike, to: PointLike) {
        g.drawLine(round(from.x()).toInt(), round(from.y()).toInt(), round(to.x()).toInt(), round(to.y()).toInt())
    }
    
    fun drawLine(center: PointLike, angle: Double, length: Double) {
        drawLine(center.project(angle, length / 2), center.project(robocode.util.Utils.normalAbsoluteAngle(angle - Math.PI), length / 2))
    }
    
    fun setColor(c: Color) {
        g.color = c
    }
    
    fun drawCircle(center: PointLike, radius: Double) {
        g.drawOval(round(center.x() - radius).toInt(), round(center.y() - radius).toInt(), round(radius * 2).toInt(), round(radius * 2).toInt())
    }
    
    fun fillCircle(center: PointLike, radius: Double) {
        g.fillOval(round(center.x() - radius).toInt(), round(center.y() - radius).toInt(), round(radius * 2).toInt(), round(radius * 2).toInt())
    }
    
    fun drawSquare(center: PointLike, width: Double) {
        g.drawRect((center.x() - width / 2).toInt(), (center.y() - width / 2).toInt(), width.toInt(), width.toInt())
    }

    fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        g.drawRect(x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }

    fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        g.fillRect(x.toInt(), y.toInt(), width.toInt(), height.toInt())
    }
    
    fun drawText(text: String, x: Double, y: Double) {
        g.drawString(text, x.toFloat(), y.toFloat())
    }

}
