package marvin.service;

import marvin.data.CompletedXferDao;
import marvin.model.CompletedXfer;
import marvin.web.history.HistoryModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

public class HistoryServiceImplTests {

    @Mock private CompletedXferDao mockCompletedXferDao;
    private HistoryServiceImpl historyService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        historyService = new HistoryServiceImpl(mockCompletedXferDao);
    }

    @Test
    public void testGetHistory_emptyHistory() {
        when(mockCompletedXferDao.selectAll()).thenReturn(Collections.emptyList());
        List<HistoryModel> history = historyService.getHistory();
        assert(history.isEmpty());
    }

    @Test
    public void testGetHistory_nonEmptyHistory() {
        when(mockCompletedXferDao.selectAll()).thenReturn(Collections.singletonList(new CompletedXfer("", "", "", 0L, LocalDateTime.now())));
        List<HistoryModel> history = historyService.getHistory();
        assert(!history.isEmpty());
    }
}
