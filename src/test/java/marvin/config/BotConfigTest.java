package marvin.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class BotConfigTest extends TestCase {

    private static File getConfigDir() {
        final String configDir = System.getenv("CONFIG_DIR");
        if (configDir != null) {
            try {
                return new File(configDir);
            } catch (Exception e) {
            }
        }
        return getConfigDirFromUserHome();
    }

    public static File getConfigDirFromUserHome() {
        return new File(System.getProperty("user.home") + File.separator + ".marvinbot");
    }

    private static File getConfigFile() {
        return new File(getConfigDir().getAbsolutePath() + File.separator + "application.conf");
    }


    @Test
    public void testGetDccPorts() {
        final BotConfig config = BotConfig.from(getConfigFile());
        assertEquals(Arrays.asList(20222, 20223, 20224), config.getDccPorts());
    }

    @Test
    public void testGetDccPublicAddress() {
        final BotConfig config = BotConfig.from(getConfigFile());
        assertEquals("localhost", config.getDccPublicAddress());
    }
}
