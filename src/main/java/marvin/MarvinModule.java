package marvin;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import marvin.config.ConfigDirectory;
import marvin.data.CompletedXferDao;
import marvin.data.sqlite3.CompletedXferSqlite3Dao;
import marvin.http.QueueResource;
import marvin.http.StatusResource;
import marvin.irc.QueueManager;
import marvin.irc.ReceiveQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MarvinModule implements Module {

    public static final Logger LOG = LoggerFactory.getLogger(MarvinModule.class);

    @Override
    public void configure(Binder binder) {
        // misc bindings
        binder.bind(QueueManager.class).to(ReceiveQueueManager.class);

        // bind DAOs
        binder.bind(CompletedXferDao.class).to(CompletedXferSqlite3Dao.class);

        // bind resources
        binder.bind(QueueResource.class);
        binder.bind(StatusResource.class);
    }

    @Provides
    @ConfigDirectory
    static File provideConfigDirectory() {
        final String configDir = System.getenv("CONFIG_DIR");
        if (configDir != null) {
            try {
                return new File(configDir);
            } catch (Exception e) {
                LOG.warn("CONFIG_DIR was specified but the directory {} could not be found", configDir);
            }
        }
        return getConfigDirFromUserHome();
    }

    public static File getConfigDirFromUserHome() {
        return new File(System.getProperty("user.home") + File.separator + ".marvinbot");
    }
}
