import moe.gensoukyo.npcspawner.region.AnyRegion2d;
import org.junit.Assert;
import org.junit.Test;

public class AnyRegionTest {
    @Test
    public void test() {
        double[][] ary = {{0,0},{10,0},{0,10}};
        AnyRegion2d region2d = new AnyRegion2d(0,2, ary);

        // 各个端点
        Assert.assertTrue(region2d.isIn(0, 1, 0));
        Assert.assertFalse(region2d.isIn(10, 1, 0));
        Assert.assertFalse(region2d.isIn(0, 1, 10));

        // 非端点线上的点
        Assert.assertTrue(region2d.isIn(0, 1, 5));
        Assert.assertTrue(region2d.isIn(5, 1, 0));
        Assert.assertFalse(region2d.isIn(5, 1, 5));

        // 图形内的点
        Assert.assertTrue(region2d.isIn(3,1,3));

        // 图形外的点
        Assert.assertFalse(region2d.isIn(11, 1, 11));
        Assert.assertFalse(region2d.isIn(-1, 1, -1));

        /* comment
        int times = 1_000_000_000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) region2d.isIn(i, 0, i);
        long t1 = System.currentTimeMillis();

        System.out.println("times : " + times + "; time: " + (t1 - t0));
         */
    }
}
