package marvin.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberUtils {

    public static final long GIGABYTE = 1024L * 1024L * 1024L;
    private static NumberFormat formatter = new DecimalFormat("#0.##");
    public static String format(long number) {
        return formatter.format(number);
    }
    public static String format(double number) {
        return formatter.format(number);
    }
}
