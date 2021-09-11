package moe.gensoukyo.npcspawner

import net.minecraft.util.math.Vec3d

class Region3d {
    @JvmField var vec1: Vec3d
    @JvmField val vec2: Vec3d

    constructor(v1: Vec3d, v2: Vec3d) {
        val fx = v1.x < v2.x
        val fy = v1.y < v2.y
        val fz = v1.z < v2.z

        if (fx && fy && fz) {
            vec1 = v1
            vec2 = v2
        } else {
            vec1 = Vec3d(if (fx) v1.x else v2.x, if (fy) v1.y else v2.y, if (fz) v1.z else v2.z)
            vec2 = Vec3d(if (fx) v2.x else v1.x, if (fy) v2.y else v1.y, if (fz) v2.z else v1.z)
        }
    }

    constructor(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double){
        val fx = x1 < x2
        val fy = y1 < y2
        val fz = z1 < z2
        vec1 = Vec3d(if (fx) x1 else x2, if (fy) y1 else y2, if (fz) z1 else z2)
        vec2 = Vec3d(if (fx) x2 else x1, if (fy) y2 else y1, if (fz) z2 else z1)
    }

    constructor(x1: Double, z1: Double, x2: Double, z2: Double) : this(x1, 0.0, z1, x2, 255.0, z2)
    constructor(vec1: Vec2d, vec2: Vec2d) : this(vec1.x, vec1.y, vec2.x, vec2.y)

    fun isVecInRegion(vec2d: Vec2d) = vecIn2d(vec2d.x, vec2d.y)

    fun isVecInRegion(v: Vec3d) = vecIn2d(v.x, v.z) && v.y > vec1.y && v.y < vec2.y

    private fun vecIn2d(x: Double, z: Double) = x > vec1.x && x < vec2.x && z > vec1.z && z < vec2.z

    fun isCoincideWith(region: Region2d): Boolean {
        val a1 = vec1
        val a2 = vec2
        val b1 = region.vec1
        val b2 = region.vec2
        return a2.x > b1.x && a1.x < b2.x && a1.z < b2.y && a2.z > b1.y
    }

    fun isCoincideWith(region: Region3d): Boolean {
        val a1 = vec1
        val a2 = vec2
        val b1: Vec3d = region.vec1
        val b2: Vec3d = region.vec2
        return a2.x > b1.x && a1.x < b2.x && a2.y > b1.y && a1.y < b2.y && a2.z > b1.z && a1.z < b2.z
    }
}