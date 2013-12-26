package lxx.events

import robocode.Bullet
import robocode.Event
import java.awt.Graphics2D

data class FireEvent(val attackerName: String, val bullet: Bullet?) : Event()