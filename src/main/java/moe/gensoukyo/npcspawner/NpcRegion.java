package moe.gensoukyo.npcspawner;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

/**
 * @author SQwatermark
 * @author MrMks
 */
public class NpcRegion {

    public String name;
    //public Region2d region;
    public String world;
    protected List<Region3d> region;
    protected Region3d rough;

    protected NpcRegion(String name, String world, List<Region3d> list) {
        this.name = name;
        this.world = world;
        this.region = ImmutableList.copyOf(list);
        findMinMax();
    }

    private void findMinMax() {
        SimpleVec3d min = null, max = null;
        for (Region3d r : region) {
            if (min == null) min = r.p1; else min = min.x < r.p1.x ? min : r.p1;
            if (max == null) max = r.p2; else max = max.x > r.p2.x ? max : r.p2;
        }
        rough = new Region3d(min, max);
    }

    public boolean isVecInRegion(SimpleVec3d vec) {
        return rough.isVecInRegion(vec) && region.stream().anyMatch(r->r.isVecInRegion(vec));
        //return f && exclude.stream().noneMatch(r -> r.isVecInRegion(vec));
    }

    /**
     * 刷怪区
     */
    public static class Spawn extends NpcRegion {

        public int density;
        public List<MobEntry> mobs;
        /*
            在初始化时会把接壤的黑名单区域存入这里，减少刷怪时需要检测的区域
         */
        private List<Region3d> exclude;
        private List<Region3d> blackList;

        public Spawn(String name, String world, List<Region3d> region, List<Region3d> exclude, List<MobEntry> mobs, int density) {
            super(name, world, region);
            this.exclude = ImmutableList.copyOf(exclude);
            this.mobs = ImmutableList.copyOf(mobs);
            this.density = density;
        }

        @Override
        public boolean isVecInRegion(SimpleVec3d vec) {
            return super.isVecInRegion(vec)
                    && exclude.stream().noneMatch(r->r.isVecInRegion(vec))
                    && blackList.stream().noneMatch(r->r.isVecInRegion(vec));
        }

        public void mapCoincideRegion(List<Black> black) {
            LinkedList<Region3d> list = new LinkedList<>();
            black.forEach(b->{
                if (b.world.equalsIgnoreCase(world) && b.rough.isRegionCoincide(rough)) {
                    for (Region3d br : b.region) {
                        if (br.isRegionCoincide(rough))
                            for (Region3d r : region) if (br.isRegionCoincide(r)) list.add(br);
                    }
                }
            });
            this.blackList = ImmutableList.copyOf(list);
        }
    }

    /**
     * 黑名单区域（安全区，不刷怪）
     */
    public static class Black extends NpcRegion {

        public boolean delete;
        public Black(String name, String world, List<Region3d> region, boolean delete) {
            super(name, world, region);
            this.delete = delete;
        }

    }

}
