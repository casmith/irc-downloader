package marvin.data;

import marvin.model.ListServer;

import java.sql.Connection;
import java.util.List;

public interface ListServerDao {
    Connection connect();
    void createTable();
    void insert(ListServer listServer);
    List<ListServer> selectAll();
    void truncate();
}
