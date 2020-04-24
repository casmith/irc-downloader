package marvin;

import com.google.inject.Binder;
import com.google.inject.Module;
import marvin.config.BotConfig;
import marvin.data.CompletedXferDao;
import marvin.data.sqlite3.CompletedXferSqlite3Dao;
import marvin.http.QueueResource;
import marvin.http.StatusResource;
import marvin.irc.QueueManager;
import marvin.irc.ReceiveQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarvinModule implements Module {

    public static final Logger LOG = LoggerFactory.getLogger(MarvinModule.class);
    private final BotConfig config;

    public MarvinModule(BotConfig config) {
        this.config = config;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(BotConfig.class).toInstance(config);

        // misc bindings
        binder.bind(QueueManager.class).to(ReceiveQueueManager.class);

        // bind DAOs
        binder.bind(CompletedXferDao.class).to(CompletedXferSqlite3Dao.class);

        // bind resources
        binder.bind(QueueResource.class);
        binder.bind(StatusResource.class);
    }
}
