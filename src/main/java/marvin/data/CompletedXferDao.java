package marvin.data;

import marvin.model.CompletedXfer;
import marvin.model.CompletedXferSummary;

import java.sql.Connection;
import java.util.List;

public interface CompletedXferDao {
    Connection connect();
    void createTable();
    void insert(CompletedXfer completedXfer);
    List<CompletedXfer> selectAll();
    CompletedXferSummary summarize();
    void truncate();
}
