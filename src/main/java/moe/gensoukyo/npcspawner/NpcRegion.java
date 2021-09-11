package moe.gensoukyo.npcspawner;

import java.util.ArrayList;

/**
 * @author SQwatermark
 */
public class NpcRegion {

    public String name;
    public Region3d region;
    public String world;

    /**
     * 刷怪区
     */
    public static class MobSpawnRegion extends NpcRegion {

        public int density;
        public ArrayList<NpcMob> mobs;
        /*
            在初始化时会把接壤的黑名单区域存入这里，减少刷怪时需要检测的区域
         */
        public ArrayList<BlackListRegion> blackList;

        public MobSpawnRegion(String name, Region3d region, int density, ArrayList<NpcMob> mobs, String world) {
            this.name = name;
            this.region = region;
            this.density = density;
            this.mobs = mobs;
            this.blackList = new ArrayList<>();
            this.world = world;
        }
    }

    /**
     * 黑名单区域（安全区，不刷怪）
     */
    public static class BlackListRegion extends NpcRegion {

        public boolean delete;
        public BlackListRegion(String name, Region3d region, boolean delete, String world) {
            this.name = name;
            this.region = region;
            this.delete = delete;
            this.world = world;
        }

    }

}
