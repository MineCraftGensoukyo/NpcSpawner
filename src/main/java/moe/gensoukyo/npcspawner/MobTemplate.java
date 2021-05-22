package moe.gensoukyo.npcspawner;

/**
 * @author MrMks
 */
public class MobTemplate {
    int tab;
    String name;
    boolean inWater;
    int timeStart, timeEnd;
    public MobTemplate(int t, String n, boolean i, int tS, int tE) {
        this.tab = t;
        this.name = n;
        this.inWater = i;
        tS = fix(tS);
        tE = fix(tE);
        this.timeStart = Math.min(tS, tE);
        this.timeEnd = Math.max(tS, tE);
    }

    private int fix(int t) {
        return Math.max(Math.min(t, 24000), 0);
    }
}
