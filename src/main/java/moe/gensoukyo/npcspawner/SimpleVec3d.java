package moe.gensoukyo.npcspawner;

import net.minecraft.util.math.Vec3d;

/**
 * @author MrMks
 */
public class SimpleVec3d {
    double x;
    double y;
    double z;

    public SimpleVec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SimpleVec3d(Vec3d vec3d) {
        this.x = vec3d.x;
        this.y = vec3d.y;
        this.z = vec3d.z;
    }

    public SimpleVec3d subtract(SimpleVec3d o) {
        return new SimpleVec3d(x - o.x, y - o.y, z - o.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVec3d vec3d = (SimpleVec3d) o;
        return Double.compare(vec3d.x, x) == 0 && Double.compare(vec3d.y, y) == 0 && Double.compare(vec3d.z, z) == 0;
    }

    @Override
    public int hashCode() {
        long j = Double.doubleToLongBits(this.x);
        int i = (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.y);
        i = 31 * i + (int)(j ^ j >>> 32);
        j = Double.doubleToLongBits(this.z);
        i = 31 * i + (int)(j ^ j >>> 32);
        return i;
    }

    @Override
    public String toString() {
        return "SpawnVec3d{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
