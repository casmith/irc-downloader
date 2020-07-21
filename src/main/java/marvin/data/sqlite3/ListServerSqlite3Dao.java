package marvin.data.sqlite3;

import marvin.config.BotConfig;
import marvin.data.DatabaseException;
import marvin.data.ListServerDao;
import marvin.model.ListServer;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListServerSqlite3Dao
    implements ListServerDao {

    private final String databasePath;

    public ListServerSqlite3Dao(String databasePath) {
        this.databasePath = databasePath;
    }

    @Inject
    public ListServerSqlite3Dao(BotConfig config) {
        this(config.getConfigDirectoryPath() + "/marvin.db");
    }

    @Override
    public Connection connect() {
        String url = "jdbc:sqlite:" + this.databasePath;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    @Override
    public void createTable() {
        this.execute("create table if not exists list_servers(\n" +
            "\tname varchar(9) not null,\n" +
            "\tnick varchar(9) not null,\n" +
            "\thost varchar(63) not null,\n" +
            "\tlast_seen int not null\n" +
            ");");

    }

    @Override
    public void insert(ListServer listServer) {
        Connection conn = connect();
        try (PreparedStatement statement = conn.prepareStatement("insert into list_servers (name, nick, host, last_seen) values (?, ?, ?, ?)")) {
            statement.setString(1, listServer.getName());
            statement.setString(2, listServer.getNick());
            statement.setString(3, listServer.getHost());
            statement.setTimestamp(4, Timestamp.valueOf(listServer.getLastSeen()));
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    @Override
    public List<ListServer> selectAll() {
        return select("SELECT name, nick, host, last_seen FROM list_servers ORDER BY last_seen DESC", new RowMapper<ListServer>() {
            @Override
            public ListServer mapRow(ResultSet rs) throws SQLException {
                return new ListServer(
                    rs.getString("name"),
                    rs.getString("nick"),
                    rs.getString("host"),
                    rs.getTimestamp("last_seen").toLocalDateTime());
            }
        });
    }

    @Override
    public void truncate() {
        this.execute("delete from list_servers; vacuum;");
    }

    public void execute(String sql) {
        Connection conn = connect();
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    public <T> List<T> select(String sql, RowMapper<T> rowMapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // loop through the result set
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed when trying to query table", e);
        }
        return results;
    }

    public abstract static class RowMapper<T> {
        public abstract T mapRow(ResultSet rs) throws SQLException;
    }
}
