import java.io.File;
import java.util.*;

/**
 * Created by clay on 9/7/17.
 */
public class ListGenerator {

    public static void main(String[] args) {
        String pathname = "/Users/clay/Rock and Metal";
//        pathname = "/Users/clay/Rock and Metal/Arch Enemy/2001 - Wages of Sin";
        ListGenerator listGenerator = new ListGenerator(new File(pathname));
        listGenerator.generate();
    }


    private final File root;
    private int fileCount = 0;
    private long bytes = 0;

    public ListGenerator(File root) {
        this.root = root;
    }

    public String generate() {

        Queue<File> queue = new LinkedList<>();

        queue.offer(this.root);

        while(!queue.isEmpty()) {
            File file = queue.remove();
            if (file.isDirectory()) {
                printDirectory(file);
                File[] files = file.listFiles();
                if (files != null) {
                    Arrays.stream(files).filter(File::isDirectory).forEach(queue::offer);
                }
            }
        }

        System.out.println(this.fileCount + " files");
        System.out.println(this.bytes / (1024.0));
        return null;
    }

    private void printDirectory(File dir) {
        boolean printedHeader = false;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                this.bytes += file.length();
                if (!file.isDirectory() && file.getName().matches("^.*\\.(mp3|wav)$")) {
                    if (!printedHeader) {
                        printedHeader = printDirectoryHeader(dir);
                    }
                    System.out.println("!bottheim " + file.getName() + " :: " + file.length() + " bytes");
                    this.fileCount++;

                }
            }
        }
    }

    private boolean printDirectoryHeader(File dir) {
        System.out.println();
        String header = dir.getAbsolutePath().substring(this.root.getAbsolutePath().length() + 1);
        String sep = String.join("", Collections.nCopies(header.length(), "-"));
        System.out.println(sep);
        System.out.println(header);
        System.out.println(sep);
        return true;
    }
}
