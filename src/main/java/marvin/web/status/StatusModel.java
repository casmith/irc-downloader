package marvin.web.status;

public class StatusModel {
    private String serverName;
    private String channel;
    private String nick;
    private String controlChannel;
    private boolean connected;
    private long files;
    private long totalBytes;

    public StatusModel() {
    }

    public StatusModel(String serverName, String channel, String nick, String controlChannel, boolean connected, long files, long totalBytes) {
        this.serverName = serverName;
        this.channel = channel;
        this.nick = nick;
        this.controlChannel = controlChannel;
        this.connected = connected;
        this.files = files;
        this.totalBytes = totalBytes;
    }

    public String getChannel() {
        return channel;
    }

    public String getNick() {
        return nick;
    }

    public String getControlChannel() {
        return controlChannel;
    }

    public boolean isConnected() {
        return connected;
    }

    public long getFiles() {
        return files;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public String getServerName() {
        return serverName;
    }
}
