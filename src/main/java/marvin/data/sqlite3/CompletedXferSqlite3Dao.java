package marvin.data.sqlite3;

import marvin.config.ConfigDirectory;
import marvin.data.CompletedXferDao;
import marvin.data.DatabaseException;
import marvin.model.CompletedXfer;
import marvin.model.CompletedXferSummary;

import javax.inject.Inject;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompletedXferSqlite3Dao implements CompletedXferDao {

    private final String databasePath;

    public CompletedXferSqlite3Dao(String databasePath) {
        this.databasePath = databasePath;
    }

    @Inject
    public CompletedXferSqlite3Dao(@ConfigDirectory File configDirectory) {
        this(configDirectory.getPath() + "/marvin.db");
    }

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

    public void createTable() {
        this.execute("create table if not exists completed_xfers(\n" +
                "\tchannel varchar(200) not null,\n" +
                "\tnick varchar(9) not null,\n" +
                "\tfile varchar(255) not null,\n" +
                "\tfilesize int not null,\n" +
                "\ttimestamp int not null\n" +
                ");");
    }

    public void insert(CompletedXfer completedXfer) {
        Connection conn = connect();
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
        return select("SELECT nick, channel, file, filesize, timestamp FROM completed_xfers", new RowMapper<CompletedXfer>() {
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

    @Override
    public CompletedXferSummary summarize() {
        List<CompletedXferSummary> results = select("SELECT count(1), sum(filesize) from completed_xfers", new RowMapper<CompletedXferSummary>() {
            @Override
            public CompletedXferSummary mapRow(ResultSet rs) throws SQLException {
                return new CompletedXferSummary(rs.getLong(1), rs.getLong(2));
            }
        });
        return results.get(0);
    }

    @Override
    public void truncate() {
        this.execute("delete from completed_xfers; vacuum;");
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
