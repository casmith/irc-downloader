package marvin;

import marvin.list.ListGenerator;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class ClientTest {

    @Mock
    private ListGenerator listGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
//
//    @Test
//    public void getAdvert() {
//        final long bytes = (long) (1024L * 1024 * 1024 * 3.1);
//        when(listGenerator.getBytes()).thenReturn(bytes);
//        when(listGenerator.getCount()).thenReturn(1000L);
//        when(listGenerator.getGeneratedDateTime()).thenReturn(LocalDateTime.of(
//                LocalDate.of(2019, Month.SEPTEMBER, 8),
//                LocalTime.of(0, 0, 0, 0)));
//        final String advert = new Client().getAdvert("mybot", listGenerator);
//        assertEquals("Type: mybot for my list of 1,000 files (3.1 GiB) Updated: Sep 8th == Using MarvinBot v0.01", advert);
//    }
}
