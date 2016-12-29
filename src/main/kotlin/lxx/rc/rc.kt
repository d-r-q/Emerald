package lxx.rc

import robocode.Bullet

fun Bullet.prettyString() = "rc.Bullet(name = ${this.name}, x = ${this.x}, y = ${this.y}, victim = ${this.victim}, heading = ${this.heading}, velocity = ${this.velocity}, power = ${this.power})"