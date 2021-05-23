package moe.gensoukyo.npcspawner;

import java.util.Objects;

public class Region3d {

    SimpleVec3d p1, p2, dif;

    public Region3d(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.p1 = new SimpleVec3d(x1, y1, z1);
        this.p2 = new SimpleVec3d(x2, y2, z2);
        fixVec();
    }

    public Region3d(SimpleVec3d pos1, SimpleVec3d pos2) {
        this.p1 = pos1;
        this.p2 = pos2;
        fixVec();
    }

    public boolean isVecInRegion(SimpleVec3d vec) {
        if (dif == null) dif = p2.subtract(p1);
        SimpleVec3d a = vec.subtract(p1);
        SimpleVec3d n = dif.subtract(a);
        return a.x > 0 && a.y > 0 && a.z > 0 && n.x > 0 && n.y > 0 && n.z > 0;
    }

    public boolean isRegionCoincide(Region3d o) {
        return p1.x < o.p2.x && p2.x > o.p1.x
                && p1.y < o.p2.y && p2.y > o.p1.y
                && p1.z < o.p2.z && p2.z > o.p1.z;
    }

    private void fixVec() {
        double[] min = new double[3];
        double[] max = new double[3];
        findMinMax(p1.x, p2.x, min, max, 0);
        findMinMax(p1.y, p2.y, min, max, 1);
        findMinMax(p1.z, p2.z, min, max, 2);
        p1 = new SimpleVec3d(min[0], min[1], min[2]);
        p2 = new SimpleVec3d(max[0], max[1], max[2]);
    }

    private void findMinMax(double a, double b, double[] min, double[] max, int i) {
        if (a <= b) {min[i] = a; max[i] = b;} else {min[i] = b; max[i] = a;}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region3d region3d = (Region3d) o;
        return Objects.equals(p1, region3d.p1) && Objects.equals(p2, region3d.p2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2);
    }

    @Override
    public String toString() {
        return "Region3d{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }
}
