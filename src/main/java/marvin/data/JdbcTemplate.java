package marvin.data;

import marvin.data.sqlite3.ListServerSqlite3Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private String url;

    public JdbcTemplate(String url) {
        this.url = url;
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void execute(String sql) {
        Connection conn = connect();
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute query", e);
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
}
