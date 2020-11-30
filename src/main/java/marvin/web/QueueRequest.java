package marvin.web;

public class QueueRequest {
    private String request;
    private String status;

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
}
