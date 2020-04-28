package marvin;

import com.google.inject.Binder;
import com.google.inject.Module;
import marvin.config.AdminPassword;
import marvin.config.BotConfig;
import marvin.config.Nick;
import marvin.data.CompletedXferDao;
import marvin.data.sqlite3.CompletedXferSqlite3Dao;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;
import marvin.irc.QueueManager;
import marvin.irc.ReceiveQueueManager;
import marvin.web.QueueResource;
import marvin.web.StatusResource;
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

        binder.bindConstant().annotatedWith(Nick.class).to(config.getNick());
        binder.bindConstant().annotatedWith(AdminPassword.class).to(config.getAdminPassword());

        // misc bindings
        binder.bind(QueueManager.class).to(ReceiveQueueManager.class);
        binder.bind(UserManager.class);
        binder.bind(ListGenerator.class);

        binder.bind(IrcBot.class).to(IrcBotImpl.class);

        // bind DAOs
        binder.bind(CompletedXferDao.class).to(CompletedXferSqlite3Dao.class);

        // bind resources
        binder.bind(QueueResource.class);
        binder.bind(StatusResource.class);
    }
}
