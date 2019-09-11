package marvin.util;

import org.junit.Test;

import static marvin.util.NumberUtils.GIGABYTE;
import static org.junit.Assert.assertEquals;

public class NumberUtilsTest {

    @Test
    public void formatNumber() {
        assertEquals("9223372036854775807", NumberUtils.format(Long.MAX_VALUE));
        assertEquals("3328599654.4", NumberUtils.format(GIGABYTE * 3.1));
        assertEquals("3.1", NumberUtils.format((GIGABYTE * 3.1) / GIGABYTE));
    }
}