package moe.gensoukyo.npcspawner;

public interface GeometryRegion {
    boolean isValid();
    boolean isIn(double x, double y, double z);
    RectRegion asRough();
}
