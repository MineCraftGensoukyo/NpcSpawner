package moe.gensoukyo.npcspawner.region

import moe.gensoukyo.npcspawner.GeometryRegion
import kotlin.math.max
import kotlin.math.min

class AnyRegion2dKt(y_min : Double, y_max : Double, private val posArray : Array<DoubleArray>) : GeometryRegion {

    private val lmtY : DoubleArray
    private val lmtX = DoubleArray(2)
    private val lmtZ = DoubleArray(2)

    private val valid : Boolean

    private var cacheRough : RectRegion3d? = null

    init {
        lmtY = if (y_min < y_max) doubleArrayOf(y_min, y_max) else doubleArrayOf(y_max, y_min)

        lmtX[0] = Double.MAX_VALUE
        lmtZ[0] = Double.MAX_VALUE
        lmtX[1] = Double.MIN_VALUE
        lmtZ[1] = Double.MIN_VALUE

        valid = posArray.all {
            val bol = it.size > 1
            if (bol) {
                lmtX[0] = min(lmtX[0], it[0])
                lmtX[1] = max(lmtX[1], it[0])
                lmtZ[0] = min(lmtX[0], it[1])
                lmtZ[1] = max(lmtX[1], it[1])
            }
            bol
        }
    }

    override fun isValid(): Boolean = valid

    override fun isIn(x: Double, y: Double, z: Double): Boolean {
        if (!valid) return false
        if (!(lmtX[0] <= x && x < lmtX[1] && lmtY[0] <= y && y < lmtY[1] && lmtZ[0] <= z && z < lmtZ[1])) return false

        var t0 : Double
        var t1 : Double
        var t2 : Double
        var d : Double

        var wn = 0
        var a = posArray.last()
        posArray.forEach { b->
            t0 = b[1] - a[1]
            t1 = b[1] - z
            t2 = a[1] - z
            if (t0 != 0.0 && (t1 * t2 < 0 || t1 * t2 == 0.0 && t1 + t2 > 0)) {
                d = x * t0 - a[0] * t1 + b[0] * t2
                wn += if (d == 0.0) {if (t0 > 0) -1 else 1} else {if(d > 0) -1 else 1}
            }
            a = b
        }
        return wn != 0
    }

    override fun asRough(): RectRegion3d =
        cacheRough ?: RectRegion3d(lmtX[0], lmtX[1], lmtZ[0], lmtZ[1], lmtY[0], lmtY[1]).also { cacheRough = it }
}