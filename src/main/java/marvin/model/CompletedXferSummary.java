package marvin.model;

public class CompletedXferSummary {
    private long count;
    private long totalBytes;

    public CompletedXferSummary(long count, long totalBytes) {
        this.count = count;
        this.totalBytes = totalBytes;
    }

    public long getCount() {
        return count;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    @Override
    public String toString() {
        return "CompletedXferSummary{" +
                "count=" + count +
                ", totalBytes=" + totalBytes +
                '}';
    }
}
