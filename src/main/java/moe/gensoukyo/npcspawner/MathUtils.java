package moe.gensoukyo.npcspawner;

public class MathUtils {
    private static final double[] ccos = new double[91];
    private static final double[] csin = new double[91];

    static {
        ccos[0] = 1;
        csin[90] = 1;
    }

    public static double[] cos_sin(int angle) {
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        return unsafe_cos_sin(angle);
    }

    public static double[] unsafe_cos_sin(int angle) {
        boolean mc = angle > 90 && angle < 270, ms = angle > 180;
        angle = angle > 270 ? 360 - angle : angle > 180 ? angle - 180 : angle > 90 ? 180 - angle : angle;
        double vc = ccos[angle], vs = csin[angle];
        if (vc == 0 && vs == 0) {
            double rad = Math.toRadians(angle);
            vc = ccos[angle] = Math.cos(rad);
            vs = csin[angle] = Math.sin(rad);
        }
        return new double[]{mc ? -vc : vc, ms ? -vs : vs};
    }
}
