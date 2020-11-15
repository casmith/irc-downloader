package marvin;

import com.google.inject.Binder;
import com.google.inject.Module;
import marvin.config.AdminPassword;
import marvin.config.BotConfig;
import marvin.config.Nick;
import marvin.data.CompletedXferDao;
import marvin.data.KnownUserDao;
import marvin.data.ListFileDao;
import marvin.data.sqlite3.CompletedXferSqlite3Dao;
import marvin.data.sqlite3.KnownUserSqlite3Dao;
import marvin.data.sqlite3.ListFileSqlite3Dao;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;
import marvin.list.ListGenerator;
import marvin.web.MarvinServletContextListener;
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
//        binder.bind(ReceiveQueueManager.class);
        binder.bind(UserManager.class);
        binder.bind(ListGenerator.class);

        binder.bind(IrcBot.class).to(IrcBotImpl.class);

        // bind DAOs
        binder.bind(CompletedXferDao.class).to(CompletedXferSqlite3Dao.class);
        binder.bind(KnownUserDao.class).to(KnownUserSqlite3Dao.class);
        binder.bind(ListFileDao.class).to(ListFileSqlite3Dao.class);

        // bind resources
        binder.bind(QueueResource.class);
        binder.bind(StatusResource.class);

        binder.bind(MarvinServletContextListener.class);
    }
}
