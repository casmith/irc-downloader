package marvin.data.sqlite3;

import marvin.config.BotConfig;
import marvin.data.DatabaseException;
import marvin.data.JdbcTemplate;
import marvin.data.ListFileDao;
import marvin.data.RowMapper;
import marvin.model.ListFile;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;

public class ListFileSqlite3Dao implements ListFileDao {

    private final JdbcTemplate jdbcTemplate;

    @Inject
    public ListFileSqlite3Dao(BotConfig config) {
        this("jdbc:sqlite:" + config.getConfigDirectoryPath() + "/marvin.db");
    }

    public ListFileSqlite3Dao(String url) {
        this(new JdbcTemplate(url));
    }

    public ListFileSqlite3Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createTable() {
        jdbcTemplate.execute("create table if not exists list_files(\n" +
            "\tname varchar(10) not null,\n" +
            "\tlast_updated int not null\n" +
            ");");
    }

    @Override
    public void insert(ListFile listFile) {
        jdbcTemplate.prepareStatement("insert into list_files (name, last_updated) values (?, ?)", (preparedStatement -> {
            try {
                preparedStatement.setString(1, listFile.getName());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(listFile.getLastUpdated()));
            } catch (SQLException e) {
                throw new DatabaseException("Failed to insert data", e);
            }
        }));
    }

    @Override
    public void update(ListFile listFile) {
        jdbcTemplate.prepareStatement("update list_files set last_updated = ? where name = ?", (preparedStatement -> {
            try {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(listFile.getLastUpdated()));
                preparedStatement.setString(2, listFile.getName());
            } catch (SQLException e) {
                throw new DatabaseException("Failed to insert data", e);
            }
        }));
    }

    @Override
    public List<ListFile> selectAll() {
        return jdbcTemplate.select("SELECT name, last_updated FROM list_files ORDER BY last_updated DESC", getRowMapper());
    }

    private RowMapper<ListFile> getRowMapper() {
        return new RowMapper<ListFile>() {
            @Override
            public ListFile mapRow(ResultSet rs) throws SQLException {
                return new ListFile(
                    rs.getString("name"),
                    rs.getTimestamp("last_updated").toLocalDateTime());
            }
        };
    }

    @Override
    public ListFile findByName(String name) {
        List<ListFile> listFiles = jdbcTemplate.query("SELECT name, last_updated FROM list_files WHERE name = ?", preparedStatement -> {
            try {
                preparedStatement.setString(1, name);
            } catch (SQLException e) {
                throw new DatabaseException("Failed to execute query", e);
            }
        }, getRowMapper());
        return listFiles.isEmpty() ? null : listFiles.get(0);
    }

    @Override
    public void truncate() {
        jdbcTemplate.execute("delete from list_files; vacuum;");
    }
}
