package marvin.data;

import marvin.model.KnownUser;

import java.util.List;

public interface KnownUserDao {
    void createTable();
    void insert(KnownUser knownUser);
    List<KnownUser> selectAll();
    void truncate();
}
