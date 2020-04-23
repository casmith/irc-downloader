package marvin.data.sqlite3;

import marvin.data.DatabaseException;
import marvin.model.CompletedXfer;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompletedXferSqlite3Dao {

    private String databasePath;

    public CompletedXferSqlite3Dao(String databasePath) {
        this.databasePath = databasePath;
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
        Connection conn = connect();
        try ( Statement statement = conn.createStatement()) {
            statement.execute("create table if not exists    completed_xfers(\n" +
                    "\tchannel varchar(200) not null,\n" +
                    "\tnick varchar(9) not null,\n" +
                    "\tfile varchar(255) not null,\n" +
                    "\tfilesize int not null,\n" +
                    "\ttimestamp int not null\n" +
                    ");");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    public void insert(CompletedXfer moros) {
        Connection conn = connect();
        try ( PreparedStatement statement = conn.prepareStatement("insert into completed_xfers (nick, channel, file, filesize, timestamp) values (?, ?, ?, ?, ?)")) {
            statement.setString(1, moros.getNick());
            statement.setString(2, moros.getChannel());
            statement.setString(3, moros.getFile());
            statement.setLong(4, moros.getFilesize());
            statement.setTimestamp(5, Timestamp.valueOf(moros.getTimestamp()));
            statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create table", e);
        }
    }

    public List<CompletedXfer> selectAll(){
        String sql = "SELECT nick, channel, file, filesize, timestamp FROM completed_xfers";

        List<CompletedXfer> completedXfers = new ArrayList<>();
        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                completedXfers.add(new CompletedXfer(
                    rs.getString("nick"),
                        rs.getString("channel"),
                        rs.getString("file"),
                        rs.getLong("filesize"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed when trying to query table", e);
        }
        return completedXfers;
    }


}
