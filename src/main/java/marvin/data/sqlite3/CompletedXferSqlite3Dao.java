package marvin.data.sqlite3;

import marvin.config.BotConfig;
import marvin.data.*;
import marvin.model.CompletedXfer;
import marvin.model.CompletedXferSummary;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;

public class CompletedXferSqlite3Dao implements CompletedXferDao {

    private final JdbcTemplate jdbcTemplate;

    @Inject
    public CompletedXferSqlite3Dao(BotConfig config) {
        this(config.getConfigDirectoryPath() + "/marvin.db");
    }

    public CompletedXferSqlite3Dao(String databasePath) {
        this(new JdbcTemplate("jdbc:sqlite:" + databasePath));
    }

    public CompletedXferSqlite3Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTable() {
        jdbcTemplate.execute("create table if not exists completed_xfers(\n" +
            "\tchannel varchar(200) not null,\n" +
            "\tnick varchar(9) not null,\n" +
            "\tfile varchar(255) not null,\n" +
            "\tfilesize int not null,\n" +
            "\ttimestamp int not null\n" +
            ");");
    }

    public void insert(CompletedXfer completedXfer) {
        Connection conn = jdbcTemplate.connect();
        try (PreparedStatement statement = conn.prepareStatement("insert into completed_xfers (nick, channel, file, filesize, timestamp) values (?, ?, ?, ?, ?)")) {
            statement.setString(1, completedXfer.getNick());
            statement.setString(2, completedXfer.getChannel());
            statement.setString(3, completedXfer.getFile());
            statement.setLong(4, completedXfer.getFilesize());
            statement.setTimestamp(5, Timestamp.valueOf(completedXfer.getTimestamp()));
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    public List<CompletedXfer> selectAll() {
        return this.select(DaoFilter.empty());
    }

    public List<CompletedXfer> select(DaoFilter filter) {
        String query = "SELECT nick, channel, file, filesize, timestamp FROM completed_xfers ORDER BY timestamp DESC";
        query = applyLimit(filter.getLimit(), query);
        return jdbcTemplate.select(query, new RowMapper<CompletedXfer>() {
            @Override
            public CompletedXfer mapRow(ResultSet rs) throws SQLException {
                return new CompletedXfer(
                    rs.getString("nick"),
                    rs.getString("channel"),
                    rs.getString("file"),
                    rs.getLong("filesize"),
                    rs.getTimestamp("timestamp").toLocalDateTime());
            }
        });
    }

    private String applyLimit(Integer limit, String query) {
        return (limit != null)
            ? query + " LIMIT " + limit
            : query;
    }

    @Override
    public CompletedXferSummary summarize() {
        List<CompletedXferSummary> results = jdbcTemplate.select("SELECT count(1), sum(filesize) from completed_xfers", new RowMapper<CompletedXferSummary>() {
            @Override
            public CompletedXferSummary mapRow(ResultSet rs) throws SQLException {
                return new CompletedXferSummary(rs.getLong(1), rs.getLong(2));
            }
        });
        return results.get(0);
    }

    @Override
    public void truncate() {
        jdbcTemplate.execute("delete from completed_xfers; vacuum;");
    }
}
