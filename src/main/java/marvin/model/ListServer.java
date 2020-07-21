package marvin.model;

import java.time.LocalDateTime;

public class ListServer {
    private String name;
    private String nick;
    private String host;
    private LocalDateTime lastSeen;

    public ListServer(String name, String nick, String host, LocalDateTime lastSeen) {
        this.name = name;
        this.nick = nick;
        this.host = host;
        this.lastSeen = lastSeen;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }

    public String getHost() {
        return host;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
}
