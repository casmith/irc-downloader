package marvin.service;

import marvin.data.CompletedXferDao;
import marvin.model.CompletedXfer;
import marvin.web.history.HistoryModel;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryServiceImpl
    implements HistoryService {

    private final CompletedXferDao completedXferDao;

    @Inject
    public HistoryServiceImpl(CompletedXferDao completedXferDao) {
        this.completedXferDao = completedXferDao;
    }

    @Override
    public List<HistoryModel> getHistory() {
        return completedXferDao.selectAll().stream()
            .map(this::toModel)
            .collect(Collectors.toList());
    }

    private HistoryModel toModel(CompletedXfer xfer) {
        return new HistoryModel(xfer.getNick(),
            xfer.getFile(),
            xfer.getFilesize(),
            toEpochMillis(xfer.getTimestamp()),
            xfer.getFilesize() > -1);
    }

    private long toEpochMillis(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
