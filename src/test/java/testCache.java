import org.junit.Test;

import java.util.Random;

public class testCache {
    public static final double[] ccos = new double[360];
    public static final double[] csin = new double[360];

    @Test
    public void test() {
        Random rg = new Random();
        long timeStart = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            double rng = Math.toRadians(rg.nextInt(360));
            double x = Math.cos(rng);
            double y = Math.sin(rng);
        }
        long timeA = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            int rng = rg.nextInt(360);
            if (ccos[rng] == 0 && csin[rng] == 0) {
                double rad = Math.toRadians(rng);
                ccos[rng] = Math.cos(rad);
                csin[rng] = Math.sin(rad);
            }
            double x = ccos[rng];
            double y = csin[rng];
        }
        long timeB = System.currentTimeMillis();

        System.out.printf("Old method time: %d\nNew method time: %d", timeA - timeStart, timeB - timeA);
    }
}
