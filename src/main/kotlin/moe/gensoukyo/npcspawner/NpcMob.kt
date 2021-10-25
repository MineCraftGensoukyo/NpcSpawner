package moe.gensoukyo.npcspawner

class NpcMob(@JvmField val tab : Int, @JvmField val name : String, @JvmField val weight : Int) {

    @JvmField var waterMob : Boolean = false
    @JvmField var timeStart : Int = 0
    @JvmField var timeEnd: Int = 24000

    fun setWaterMob(wm : Boolean) {
        this.waterMob = wm
    }

    fun setTimeIndex(timeStart: Int, timeEnd: Int) {
        this.timeStart = timeStart
        this.timeEnd = timeEnd
    }
}