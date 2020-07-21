package marvin.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class RowMapper<T> {
    public abstract T mapRow(ResultSet rs) throws SQLException;
}
