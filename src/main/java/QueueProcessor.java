public class QueueProcessor implements Runnable {

    private int quiet = 0;
    private DownloadQueue downloadQueue;

    public static void main(String[] args) {
        DownloadQueue downloadQueue = new DownloadQueue();
        downloadQueue.enqueue("!someguy Metallica - Am I Evil.mp3");
        downloadQueue.enqueue("!someguy Metallica - Ride the Lightning.mp3");
        downloadQueue.enqueue("!someguy Metallica - Master of Puppets.mp3");

        QueueProcessor queueProcessor = new QueueProcessor(downloadQueue, 1000);
        queueProcessor.run();
    }

    public QueueProcessor(DownloadQueue downloadQueue, int quiet) {
        this.quiet = quiet;
        this.downloadQueue = downloadQueue;
    }

    @Override
    public void run() {

        while (true) {
            processQueue();
            sleep(quiet);
        }
    }

    public void processQueue() {
        FileRequest request = downloadQueue.request();
        System.out.println("Requesting " + request.toString());
    }

    private void sleep(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
