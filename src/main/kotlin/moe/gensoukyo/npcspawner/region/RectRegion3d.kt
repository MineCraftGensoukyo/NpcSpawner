package moe.gensoukyo.npcspawner.region

import moe.gensoukyo.npcspawner.GeometryRegion

class RectRegion3d(
    private var x1 : Double,
    private var y1 : Double,
    private var z1 : Double,
    private var x2 : Double,
    private var y2 : Double,
    private var z2 : Double) : GeometryRegion {

    init {
        if (x1 > x2) x1 = x2.also { x2 = x1 }
        if (y1 > y2) y1 = y2.also { y2 = y1 }
        if (z1 > z2) z1 = z2.also { z2 = z1 }
    }

    constructor(x1: Double, z1: Double, x2: Double, z2: Double) : this(x1, 0.0, z1, x2, 255.0, z2)

    override fun isInExact(x: Double, y: Double, z: Double) : Boolean =
        x1 <= x && x < x2 && y1 <= y && y < y2 && z1 <= z && z < z2

    override fun toRoughRegion() = this

    fun isCoincideWith(rr : RectRegion3d) : Boolean =
        x1 < rr.x2 && x2 > rr.x1 && y1 < rr.y2 && y2 > rr.y1 && z1 < rr.z2 && z2 > rr.z1
}