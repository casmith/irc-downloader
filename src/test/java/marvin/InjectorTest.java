package marvin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import marvin.config.BotConfig;
import marvin.irc.ReceiveQueueManager;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class InjectorTest {

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
    public void testInjector() {
        final BotConfig config = BotConfig.from(getConfigFile());
        final MarvinModule marvinModule = new MarvinModule(config);
        Injector injector = Guice.createInjector(marvinModule);
        ReceiveQueueManager instance = injector.getInstance(ReceiveQueueManager.class);
        assertNotNull(instance);
    }
}
