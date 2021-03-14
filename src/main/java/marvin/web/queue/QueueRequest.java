package marvin.web.queue;

public class QueueRequest {
    private final String request;
    private final String status;

    public QueueRequest() {
        this(null, null);
    }

    public QueueRequest(String request, String status) {
        this.request = request;
        this.status = status;
    }

    public String getRequest() {
        return request;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "QueueRequest{" +
            "request='" + request + '\'' +
            ", status='" + status + '\'' +
            '}';
    }
}
