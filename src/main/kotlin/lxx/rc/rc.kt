package lxx.rc

import robocode.Bullet

fun Bullet.prettyString() = "rc.Bullet(name = ${this.getName()}, x = ${this.getX()}, y = ${this.getY()}, victim = ${this.getVictim()}, heading = ${this.getHeading()}, velocity = ${this.getVelocity()}, power = ${this.getPower()})"