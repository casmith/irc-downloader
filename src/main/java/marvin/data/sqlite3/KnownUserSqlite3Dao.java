package marvin.data.sqlite3;

import marvin.config.BotConfig;
import marvin.data.DatabaseException;
import marvin.data.JdbcTemplate;
import marvin.data.KnownUserDao;
import marvin.data.RowMapper;
import marvin.model.KnownUser;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;

public class KnownUserSqlite3Dao
    implements KnownUserDao {

    private final JdbcTemplate jdbcTemplate;

    public KnownUserSqlite3Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Inject
    public KnownUserSqlite3Dao(BotConfig config) {
        this("jdbc:sq" +
            "lite:" + config.getConfigDirectoryPath() + "/marvin.db");
    }

    public KnownUserSqlite3Dao(String url) {
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
    public void insert(KnownUser knownUser) {
        Connection conn = jdbcTemplate.connect();
        try (PreparedStatement statement = conn.prepareStatement("insert into list_servers (name, nick, host, last_seen) values (?, ?, ?, ?)")) {
            statement.setString(1, knownUser.getName());
            statement.setString(2, knownUser.getNick());
            statement.setString(3, knownUser.getHost());
            statement.setTimestamp(4, Timestamp.valueOf(knownUser.getLastSeen()));
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    @Override
    public List<KnownUser> selectAll() {
        return jdbcTemplate.select("SELECT name, nick, host, last_seen FROM list_servers ORDER BY last_seen DESC", new RowMapper<KnownUser>() {
            @Override
            public KnownUser mapRow(ResultSet rs) throws SQLException {
                return new KnownUser(
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
