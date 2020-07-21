package marvin.data.sqlite3;

import marvin.config.BotConfig;
import marvin.data.DatabaseException;
import marvin.data.JdbcTemplate;
import marvin.data.ListServerDao;
import marvin.data.RowMapper;
import marvin.model.ListServer;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;

public class ListServerSqlite3Dao
    implements ListServerDao {

    private final JdbcTemplate jdbcTemplate;

    public ListServerSqlite3Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Inject
    public ListServerSqlite3Dao(BotConfig config) {
        this("jdbc:sq" +
            "lite:" + config.getConfigDirectoryPath() + "/marvin.db");
    }

    @Inject
    public ListServerSqlite3Dao(String url) {
        this(new JdbcTemplate(url));
    }

    @Override
    public void createTable() {
        jdbcTemplate.execute("create table if not exists list_servers(\n" +
            "\tname varchar(9) not null,\n" +
            "\tnick varchar(9) not null,\n" +
            "\thost varchar(63) not null,\n" +
            "\tlast_seen int not null\n" +
            ");");
    }

    @Override
    public void insert(ListServer listServer) {
        Connection conn = jdbcTemplate.connect();
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
        return jdbcTemplate.select("SELECT name, nick, host, last_seen FROM list_servers ORDER BY last_seen DESC", new RowMapper<ListServer>() {
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
        jdbcTemplate.execute("delete from list_servers; vacuum;");
    }
}
