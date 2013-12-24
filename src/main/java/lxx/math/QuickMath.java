package lxx.math;

public final class QuickMath {

    public static final double PI = 3.1415926535897932384626433832795D;
    public static final double TWO_PI = 6.2831853071795864769252867665590D;
    public static final double HALF_PI = 1.5707963267948966192313216916398D;

    /* Setting for trig */
    /* Must be power of 2 */
    private static final int TRIG_HIGH_DIVISIONS = 131072;
    private static final int SINE_TABLE_DELTA1 = (TRIG_HIGH_DIVISIONS - 1);
    private static final double SIN_TABLE_DELTA2 = 1.25 * TRIG_HIGH_DIVISIONS;
    private static final double ACOS_K = SINE_TABLE_DELTA1 / 2;
    private static final double ACOS_TABLE_DELTA = (ACOS_K + 0.5);
    private static final double K = TRIG_HIGH_DIVISIONS / TWO_PI;
    private static final double TAN_K = TRIG_HIGH_DIVISIONS / PI;

    /* Lookup tables */
    private static final double[] sineTable = new double[TRIG_HIGH_DIVISIONS];
    private static final double[] acosTable = new double[TRIG_HIGH_DIVISIONS];
    private static final double[] tanTable = new double[TRIG_HIGH_DIVISIONS];

    static {
        init();
    }

    /* Hide the constructor */
    private QuickMath() {
    }

    /**
     * Initializing the lookup table
     */
    public static void init() {
        if (sineTable[1] == 0) {
            for (int i = 0; i < TRIG_HIGH_DIVISIONS; i++) {
                sineTable[i] = Math.sin(i / K);
                acosTable[i] = Math.acos(i / ACOS_K - 1);
                tanTable[i] = Math.tan(i / TAN_K);
            }
        }
    }

    public static double sin(double value) {
        return sineTable[(int) (((value * K + 0.5) % TRIG_HIGH_DIVISIONS + TRIG_HIGH_DIVISIONS)) & SINE_TABLE_DELTA1];
    }

    public static double cos(double value) {
        return sineTable[(int) (((value * K + 0.5) % TRIG_HIGH_DIVISIONS + SIN_TABLE_DELTA2)) & SINE_TABLE_DELTA1];
    }

    public static double tan(double value) {
        return tanTable[(int) (((value * TAN_K + 0.5) % TRIG_HIGH_DIVISIONS + TRIG_HIGH_DIVISIONS)) & (TRIG_HIGH_DIVISIONS - 1)];
    }

    public static double asin(double value) {
        return HALF_PI - acosTable[(int) (value * ACOS_K + ACOS_TABLE_DELTA)];
    }

    public static double acos(double value) {
        return acosTable[(int) (value * ACOS_K + ACOS_TABLE_DELTA)];
    }

}
