package marvin.data.sqlite3;

import marvin.config.BotConfig;
import marvin.data.DatabaseException;
import marvin.data.JdbcTemplate;
import marvin.data.QueueEntryDao;
import marvin.data.RowMapper;
import marvin.model.QueueEntry;
import marvin.queue.QueueStatus;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;

public class QueueEntrySqlite3Dao
    implements QueueEntryDao {

    private JdbcTemplate jdbcTemplate;

    @Inject
    public QueueEntrySqlite3Dao(BotConfig config) {
        this("jdbc:sqlite:" + config.getConfigDirectoryPath() + "/marvin.db");
    }

    public QueueEntrySqlite3Dao(String url) {
        this(new JdbcTemplate(url));
    }

    public QueueEntrySqlite3Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createTable() {
        jdbcTemplate.execute("create table if not exists queue_entries(\n" +
            "\tname varchar(9) not null,\n" +
            "\tbatch varchar(200) not null,\n" +
            "\trequest_string varchar(4000) not null,\n" +
            "\tstatus varchar(16) not null,\n" +
            "\tchannel varchar(200) not null,\n" +
            "\ttimestamp int not null\n" +
            ");");
    }

    @Override
    public void delete(QueueEntry queueEntry) {
        jdbcTemplate.execute("delete from queue_entries where name = ? and request_string like ?", queueEntry.getName(), "%" + queueEntry.getRequestString() + "%");
    }

    @Override
    public List<QueueEntry> findByNickAndStatus(final String nick, final QueueStatus status) {
        return jdbcTemplate.query("SELECT name, batch, request_string, status, channel, timestamp FROM queue_entries WHERE name = ? and status = ? ORDER BY timestamp", new Object[]{nick, status.toString()}, new QueueEntryRowMapper());
    }

    @Override
    public List<QueueEntry> findByStatus(QueueStatus status) {
        return jdbcTemplate.query("SELECT name, batch, request_string, status, channel, timestamp FROM queue_entries WHERE status = ? ORDER BY name, timestamp", new Object[]{status.toString()}, new QueueEntryRowMapper());
    }

    @Override
    public void updateStatus(QueueEntry queueEntry, QueueStatus requested) {
        jdbcTemplate.execute("UPDATE queue_entries SET status = ? WHERE request_string = ?", requested.toString(), queueEntry.getRequestString());
    }

    @Override
    public QueueEntry find(final String nick, final String requestLike) {
        List<QueueEntry> entries = jdbcTemplate.query("SELECT name, batch, request_string, status, channel, timestamp FROM queue_entries WHERE name = ? and request_string LIKE ? ORDER BY timestamp", new Object[]{nick, "%" + requestLike + "%"}, new QueueEntryRowMapper());
        return !entries.isEmpty() ? entries.get(0) : null;
    }

    @Override
    public void insert(QueueEntry queueEntry) {
        Connection conn = jdbcTemplate.connect();
        try (PreparedStatement statement = conn.prepareStatement("insert into queue_entries (name, batch, request_string, status, channel, timestamp) values (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, queueEntry.getName());
            statement.setString(2, queueEntry.getBatch());
            statement.setString(3, queueEntry.getRequestString());
            statement.setString(4, queueEntry.getStatus());
            statement.setString(5, queueEntry.getChannel());
            statement.setTimestamp(6, Timestamp.valueOf(queueEntry.getTimestamp()));
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    @Override
    public void resetAll() {
        jdbcTemplate.execute("update queue_entries set status = 'PENDING'");
    }

    @Override
    public List<QueueEntry> selectAll() {
        return jdbcTemplate.select("SELECT name, batch, request_string, status, channel, timestamp FROM queue_entries ORDER BY timestamp", new QueueEntryRowMapper());
    }

    @Override
    public List<QueueEntry> findByBatch(String batch) {
        return jdbcTemplate.query("SELECT name, batch, request_string, status, channel, timestamp FROM queue_entries WHERE batch = ? ORDER BY timestamp", new Object[]{batch}, new QueueEntryRowMapper());
    }

    @Override
    public void truncate() {
        jdbcTemplate.execute("delete from queue_entries; vacuum;");
    }

    public static class QueueEntryRowMapper extends RowMapper<QueueEntry> {

        @Override
        public QueueEntry mapRow(ResultSet rs) throws SQLException {
            return new QueueEntry(
                rs.getString("name"),
                rs.getString("batch"),
                rs.getString("request_string"),
                rs.getString("status"),
                rs.getString("channel"),
                rs.getTimestamp("timestamp").toLocalDateTime());
        }
    }
}
