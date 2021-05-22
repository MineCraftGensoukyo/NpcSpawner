package moe.gensoukyo.npcspawner;

/**
 * @author MrMks
 */
public class MobEntry {
    MobTemplate template;
    private int weight;
    private final int weightSrc;

    public MobEntry(MobTemplate tmp, int weight) {
        this.template = tmp;
        this.weight = this.weightSrc = Math.max(weight, 0);
    }

    /**
     * 支持新算法用的方法
     * 该方法会改变weight的值，即，此方法在返回0之前，每次调用的返回值都会改变
     * @return the weight of this mob at this time.
     */
    public int getWeight() {
        if (weight > 0) return weight--;
        else return (weight = 0);
    }

    public int getWeightSilence() {
        return weight;
    }

    public void resetWeight() {
        this.weight = weightSrc;
    }
}
