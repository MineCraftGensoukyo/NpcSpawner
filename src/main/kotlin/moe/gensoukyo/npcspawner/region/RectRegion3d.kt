package moe.gensoukyo.npcspawner.region

import moe.gensoukyo.npcspawner.RectRegion

class RectRegion3d (
    private var x1 : Double,
    private var y1 : Double,
    private var z1 : Double,
    private var x2 : Double,
    private var y2 : Double,
    private var z2 : Double) : RectRegion {

    init {
        if (x1 > x2) x1 = x2.also { x2 = x1 }
        if (y1 > y2) y1 = y2.also { y2 = y1 }
        if (z1 > z2) z1 = z2.also { z2 = z1 }
    }

    constructor(x1: Double, z1: Double, x2: Double, z2: Double) : this(x1, 0.0, z1, x2, 255.0, z2)

    override fun isValid() : Boolean = true

    override fun isIn(x: Double, y: Double, z: Double) : Boolean =
        x1 <= x && x < x2 && y1 <= y && y < y2 && z1 <= z && z < z2

    override fun minX() = x1
    override fun maxX() = x2
    override fun minY() = y1
    override fun maxY() = y2
    override fun minZ() = z1
    override fun maxZ() = z2
}