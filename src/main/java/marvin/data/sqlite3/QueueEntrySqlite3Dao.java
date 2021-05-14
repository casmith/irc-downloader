package marvin.data.sqlite3;

import marvin.data.DatabaseException;
import marvin.data.JdbcTemplate;
import marvin.data.QueueEntryDao;
import marvin.data.RowMapper;
import marvin.model.QueueEntry;

import java.sql.*;
import java.util.List;

public class QueueEntrySqlite3Dao
    implements QueueEntryDao {

    private JdbcTemplate jdbcTemplate;

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
    public List<QueueEntry> selectAll() {
        return jdbcTemplate.select("SELECT name, batch, request_string, status, channel, timestamp FROM queue_entries ORDER BY timestamp DESC", new RowMapper<QueueEntry>() {
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
        });

    }

    @Override
    public void truncate() {
        jdbcTemplate.execute("delete from queue_entries; vacuum;");
    }
}
