package marvin.data;

public class DaoFilter {
    private static DaoFilter EMPTY_FILTER = new DaoFilter();
    private Integer limit;

    public DaoFilter() {
    }

    public DaoFilter(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public static DaoFilter empty() {
        return EMPTY_FILTER;
    }
}
