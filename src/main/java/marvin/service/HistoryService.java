package marvin.service;

import marvin.web.history.HistoryModel;

import java.util.List;

public interface HistoryService {
    List<HistoryModel> getHistory();
}
