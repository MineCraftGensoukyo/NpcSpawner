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
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isIn(double x, double y, double z) {
        // valid test
        if (!valid) return false;
        // rough region test
        if (!(x_min <= x && x < x_max && y_min <= y && y < y_max && z_min <= z && z < z_max)) return false;

        // count of the number
        int wn = 0;
        // temp variables
        double t0, t1, t2;
        // a[0] -> x_a, a[1] -> y_a
        double[] a = pos_l[pos_l.length - 1];
        for (double[] b : pos_l) {
            // 这是转角法的的跨线法的一种实现
            // 这里，我们计算在跨越线在测试点P的右侧时上跨+1，下跨-1; 在P左侧时下跨+1，上跨-1;
            // P与跨越线重合时，认为跨越线在P的左侧, 使图形左侧线段属于图形内部;
            // 对于构成跨越线的两端点，当其y坐标与测试点y坐标相同时，认为端点在测试点下方, 使图形下侧线属于图形内部

            // t0 = y_b - y_a, t1 = y_a - z, t2 = y_b - z
            // t0 != 0, 不平行; t1 * t2 < 0, 异侧; t1 * t2 == 0 && t1 + t2 > 0, 线经过一点, 另一点在线上侧，即下线在图形内部，上线在图形外部
            if ((t0 = b[1] - a[1]) != 0 && ((t1 = b[1] - z) * (t2 = a[1] - z) < 0 || (t1 * t2 == 0 && t1 + t2 > 0))) {
                // 计算在向量(t0, x_a - x_b)上，(x,y)的模与(x_a, y_a)的模之差
                double d = (x * t0 - a[0] * t1 + b[0] * t2);
                // d == 0时, 测试点在a,b连线上, 认为线在点左侧，此时, t0 > 0向上跨过，-1，t0 < 0向下过，+1
                // d > 0时, t0 > 0, 线在点左侧上跨, -1, t0 < 0, 线在点右侧下跨, -1;
                // d < 0时, t0 > 0, 线在点右侧上跨, +1, t0 < 0, 线在点左侧下跨, +1;
                wn += d == 0 ? (t0 > 0 ? -1 : +1) : (d > 0 ? -1 : +1);

                // 下面代码仅考虑点在右侧的情况, 即 d > 0 && t0 < 0 和 d < 0 && t0 > 0 两种情况
                // 计数上可以不严格按照上+1下-1计算, 双向加减相同的值即可
                // wn += (d > 0 && t0 < 0 || d < 0 && t0 > 0) ? t0 > 0 ? 1 : -1 : 0;
            }
            a = b;
        }
        return wn != 0;
    }

    @Override
    public RectRegion3d asRough() {
        if (cacheRough == null) cacheRough = new RectRegion3d(x_min, y_min, z_min, x_max, y_max, z_max);
        return cacheRough;
    }
}
