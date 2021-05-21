package marvin;

import com.google.inject.Binder;
import com.google.inject.Module;
import marvin.config.AdminPassword;
import marvin.config.BotConfig;
import marvin.config.Nick;
import marvin.config.RmqHost;
import marvin.data.CompletedXferDao;
import marvin.data.KnownUserDao;
import marvin.data.ListFileDao;
import marvin.data.QueueEntryDao;
import marvin.data.sqlite3.CompletedXferSqlite3Dao;
import marvin.data.sqlite3.KnownUserSqlite3Dao;
import marvin.data.sqlite3.ListFileSqlite3Dao;
import marvin.data.sqlite3.QueueEntrySqlite3Dao;
import marvin.irc.IncomingFileTransferListener;
import marvin.irc.IrcBot;
import marvin.irc.IrcBotImpl;
import marvin.irc.events.EventSource;
import marvin.list.ListGenerator;
import marvin.messaging.Producer;
import marvin.messaging.RabbitMqProducer;
import marvin.service.HistoryService;
import marvin.service.HistoryServiceImpl;
import marvin.web.MarvinServletContextListener;
import marvin.web.history.HistoryResource;
import marvin.web.queue.QueueResource;
import marvin.web.server.ServerResource;
import marvin.web.status.StatusResource;

public class MarvinModule implements Module {

    private final BotConfig config;

    public MarvinModule(BotConfig config) {
        this.config = config;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(BotConfig.class).toInstance(config);
        binder.bind(String.class).annotatedWith(RmqHost.class)
            .toInstance(config.getRmqHost());

        binder.bindConstant().annotatedWith(Nick.class).to(config.getNick());
        binder.bindConstant().annotatedWith(AdminPassword.class).to(config.getAdminPassword());

        // misc bindings
//        binder.bind(ReceiveQueueManager.class);
        binder.bind(UserManager.class);
        binder.bind(ListGenerator.class);

        binder.bind(IncomingFileTransferListener.class);

        binder.bind(IrcBot.class).to(IrcBotImpl.class);

        // bind DAOs
        binder.bind(CompletedXferDao.class).to(CompletedXferSqlite3Dao.class);
        binder.bind(KnownUserDao.class).to(KnownUserSqlite3Dao.class);
        binder.bind(ListFileDao.class).to(ListFileSqlite3Dao.class);
        binder.bind(QueueEntryDao.class).to(QueueEntrySqlite3Dao.class);

        // services
        binder.bind(HistoryService.class).to(HistoryServiceImpl.class);

        // bind resources
        binder.bind(HistoryResource.class);
        binder.bind(QueueResource.class);
        binder.bind(StatusResource.class);
        binder.bind(ServerResource.class);

        binder.bind(Producer.class).to(RabbitMqProducer.class);

        binder.bind(MarvinServletContextListener.class);
    }
}
