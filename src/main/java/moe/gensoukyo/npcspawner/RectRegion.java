package moe.gensoukyo.npcspawner;

public interface RectRegion extends GeometryRegion {
    double minX();
    double maxX();
    double minY();
    double maxY();
    double minZ();
    double maxZ();

    default boolean isCoincide(RectRegion other) {
        return this.minX() < other.maxX() && this.maxX() > other.minX()
                && this.minY() < other.maxY() && this.maxY() > other.minY()
                && this.minZ() < other.maxZ() && this.maxZ() > other.minZ();
    }

    @Override
    default RectRegion asRough() {
        return this;
    }
}
