package marvin.data;

import marvin.model.CompletedXfer;
import marvin.model.CompletedXferSummary;

import java.util.List;

public interface CompletedXferDao {
    void createTable();
    void insert(CompletedXfer completedXfer);
    List<CompletedXfer> selectAll();
    List<CompletedXfer> select(DaoFilter filter);
    CompletedXferSummary summarize();
    void truncate();
}
