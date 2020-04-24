package marvin.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import marvin.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BotConfig {
    private final String adminPassword;
    private final String configDirectoryPath;
    private final String controlChannel;
    private final String downloadDirectory;
    private final String list;
    private final String listRoot;
    private final String nick;
    private final String password;
    private final int port;
    private final String requestChannel;
    private final String server;

    public BotConfig(Config config, String configDirectoryPath) {
        Config ircConfig = config.getConfig("irc");
        this.adminPassword = ircConfig.getString("adminpw");
        this.configDirectoryPath = configDirectoryPath;
        this.controlChannel = getString(ircConfig, "controlChannel");
        this.downloadDirectory = getString(ircConfig, "downloadDirectory");
        this.list = ircConfig.getString("list");
        this.listRoot = ircConfig.getString("listRoot");
        this.nick = ircConfig.getString("nick");
        this.password = getString(ircConfig, "password");
        this.port = ircConfig.getInt("port");
        this.requestChannel = ircConfig.getString("requestChannel");
        this.server = getString(ircConfig, "server");
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getConfigDirectoryPath() {
        return configDirectoryPath;
    }

    public String getControlChannel() {
        return controlChannel;
    }

    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public String getList() {
        return list;
    }

    public String getListRoot() {
        return listRoot;
    }

    public String getNick() {
        return nick;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getRequestChannel() {
        return requestChannel;
    }

    public String getServer() {
        return server;
    }

    public boolean isFeatureEnabled(String featureName) {
        return false;
    }

//    private boolean isFeatureEnabled(String feature) {
//        String key = "features." + feature;
//        return config.hasPath(key) && config.getBoolean(key);
//    }

    private String getString(Config config, String path) {
        if (config.hasPath(path)) {
            return config.getString(path);
        }
        return null;
    }

    public static BotConfig from(File configFile) {
        // make the parent directory
        final File configDirectory = configFile.getParentFile();
        configDirectory.mkdirs();
        setupLocalConfigFile(configFile);
        return new BotConfig(ConfigFactory.parseFile(configFile), configDirectory.getAbsolutePath());
    }

    private static void setupLocalConfigFile(File configFile) {
        InputStream stream = Client.class.getClassLoader().getResourceAsStream("application.conf");
        if (stream != null) {
            if (!configFile.exists()) {
                copyStreamToFile(stream, configFile);
            }
        }
    }

    private static void copyStreamToFile(InputStream stream, File configFile) {
        try {
            int readBytes;
            byte[] buffer = new byte[4096];
            FileOutputStream resStreamOut = new FileOutputStream(configFile);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
