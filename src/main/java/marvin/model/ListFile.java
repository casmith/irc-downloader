package marvin.model;

import java.time.LocalDateTime;

public class ListFile {

    private final String name;
    private final LocalDateTime lastUpdated;

    public ListFile(String name, LocalDateTime lastUpdated) {
        this.name = name;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
