package marvin.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    public void execute(String sql, Object... params) {
        Connection conn = connect();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            setParams(statement, params);
            statement.execute();
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

    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) {
        Connection conn = connect();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            consumer.accept(statement);
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert data", e);
        }
    }

    public <T> List<T> query(String sql, Consumer<PreparedStatement> consumer, RowMapper<T> rowMapper) {
        Connection conn = connect();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            List<T> results = new ArrayList<>();
            consumer.accept(statement);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query data", e);
        }
    }
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        Connection conn = connect();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParams(stmt, args);

            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query data", e);
        }
    }

    private void setParams(PreparedStatement stmt, Object[] args) throws SQLException {
        int i = 0;
        for (Object arg : args) {
            if (arg instanceof String) {
                stmt.setString(++i, (String) arg);
            } else if (arg instanceof Integer) {
                stmt.setInt(++i, (int)arg);
            } else {
                throw new DatabaseException("Unsupported Parameter type");
            }
            // todo: support more than strings and ints
        }
    }
}
