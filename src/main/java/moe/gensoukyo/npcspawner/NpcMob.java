package moe.gensoukyo.npcspawner;

/**
 * NPC怪物信息
 */
public class NpcMob {

    int tab;
    String name;
    int weight;
    boolean waterMob = false;
    int timeStart = 0;
    int timeEnd = 24000;

    /**
     * @param tab 服务端存储的NPC的tab
     * @param name NPC的名字
     * @param weight NPC在刷怪区中的权重
     */
    public NpcMob(int tab, String name, double weight) {
        this.tab = tab;
        this.name = name;
        this.weight = (int) (weight * 100);
    }

    public void setWaterMob(boolean waterMob) {
        this.waterMob = waterMob;
    }

    public void setTimeIndex(int timeStart, int timeEnd) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }
}
