import moe.gensoukyo.npcspawner.region.AnyRegion2d;
import org.junit.Assert;
import org.junit.Test;

public class AnyRegionTest {
    @Test
    public void test() {
        double[][] ary = {{0,0},{10,0},{0,10}};
        AnyRegion2d region2d = new AnyRegion2d(0,2, ary);
        Assert.assertTrue(region2d.isInExact(0, 1, 0));
        Assert.assertTrue(region2d.isInExact(0, 1, 5));
        Assert.assertFalse(region2d.isInExact(5, 1, 5));
        Assert.assertFalse(region2d.isInExact(0, 1, 10));
        Assert.assertFalse(region2d.isInExact(0, 1, 11));
        Assert.assertTrue(region2d.isInExact(5, 1, 0));

    }
}
