package moe.gensoukyo.npcspawner;

import moe.gensoukyo.npcspawner.region.RectRegion3d;

public interface GeometryRegion {
    boolean isInExact(double x, double y, double z);
    RectRegion3d toRoughRegion();
}
