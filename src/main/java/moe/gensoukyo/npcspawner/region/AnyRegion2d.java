package moe.gensoukyo.npcspawner.region;

import moe.gensoukyo.npcspawner.GeometryRegion;

public class AnyRegion2d implements GeometryRegion {

    private final double y_min, y_max;
    private final double x_min, x_max;
    private final double z_min, z_max;

    private final double[][] pos_l;

    private final boolean valid;
    private RectRegion3d cacheRough;

    public AnyRegion2d(double min_y, double max_y, double[][] ary) {
        this.y_min = min_y;
        this.y_max = max_y;

        boolean f = true;
        double t_mx, t_nx, t_mz, t_nz;
        t_mx = t_mz = Double.MIN_VALUE;
        t_nx = t_nz = Double.MAX_VALUE;
        for (double[] pos : ary) {
            if (!(f = pos.length > 1)) {
                break;
            } else {
                t_mx = Math.max(t_mx, pos[0]);
                t_nx = Math.min(t_nx, pos[0]);
                t_mz = Math.max(t_mz, pos[1]);
                t_nz = Math.min(t_nz, pos[1]);
            }
        }
        valid = f;
        x_min = t_nx;
        x_max = t_mx;
        z_min = t_nz;
        z_max = t_mz;
        pos_l = ary;
    }

    @Override
    public boolean isInExact(double x, double y, double z) {
        if (!(valid && x_min <= x && x <= x_max && y_min <= y && y <= y_max && z_min <= z && z <= z_max)) return false;
        int wn = 0;
        double t0, t1, t2;
        double[] a, b;
        for (int i = 0; i < pos_l.length; i++) {
            a = pos_l[i]; b = pos_l[i + 1 == pos_l.length ? 0 : i + 1];
            if ((t0 = b[1] - a[1]) != 0 && ((t1 = b[1] - z) * (t2 = a[1] - z) < 0 || (t1 * t2 == 0 && t1 + t2 > 0))) {
                double d = (x * t0 - a[0] * t1 + b[0] * t2);
                wn += d == 0 ? (t0 > 0 ? -1 : +1) : (d > 0 ? -1 : +1);
            }
        }
        return wn != 0;
    }

    @Override
    public RectRegion3d toRoughRegion() {
        if (cacheRough == null) cacheRough = new RectRegion3d(x_min, y_min, z_min, x_max, y_max, z_max);
        return cacheRough;
    }
}
