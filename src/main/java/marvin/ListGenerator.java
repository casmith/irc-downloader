package marvin;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Arrays;

import static java.text.MessageFormat.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE;

public class ListGenerator {
    private final String nick;
    private long bytes = 0;
    private long count = 0;
    private LocalDateTime generatedDateTime;

    public ListGenerator(String nick) {
        this.nick = nick;
    }

    public long getCount() {
        return count;
    }

    public long getBytes() {
        return bytes;
    }

    public LocalDateTime getGeneratedDateTime() {
        return generatedDateTime;
    }

    /**
     * @param rootDirectory Directory to generate the list for
     */
    public void generateList(File rootDirectory) {

        generatedDateTime = LocalDateTime.now();
        try (final ListWriter listWriter = new ListWriter(new FileWriter(getFileName(this.nick)))) {
            generateList(rootDirectory, listWriter);
        } catch (Exception e) {
            throw new ListGeneratorException("Failed to generate list", e);
        }
    }

    private void generateList(File rootDirectory, ListWriter listWriter) {
        if (rootDirectory.isDirectory()) {
            File[] files = rootDirectory.listFiles(ListGenerator::filterFiles);
            if (files != null && files.length > 0) {
                listWriter.writeLine();
                listWriter.writeLine("=============================================");
                listWriter.writeLine(rootDirectory);
                listWriter.writeLine("=============================================");
                writeFiles(files, listWriter);
            }

            File[] dirs = rootDirectory.listFiles(File::isDirectory);
            if (dirs.length > 0) {
                Arrays.stream(dirs)
                        .sorted()
                        .forEach(dir -> generateList(dir, listWriter));
            }
        }
    }

    private static String getFileName(String nick) {
        String isoDate = now().format(ISO_DATE);
        return format("{0}-Default({1})-MB.txt", nick, isoDate);
    }

    private static boolean filterFiles(File pathname) {
        return !pathname.isDirectory() && pathname.getName().endsWith(".mp3");
    }

    public void printStatistics() {
        System.out.println(count + " files");
        double mb = bytes / 1024.0 / 1024.0 / 1024.0;
        DecimalFormat format = new DecimalFormat("##.00");
        System.out.println(format.format(mb) + " GiB");
    }

    private void writeFiles(File[] files, ListWriter writer) {
        Arrays.stream(files)
                .sorted()
                .forEach(file1 -> {
                    bytes += file1.length();
                    count++;
                    writer.writeLine("!" + nick + " " + file1);
                });
    }


    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        String nick = config.getString("irc.nick");
        ListGenerator listGenerator = new ListGenerator(nick);
        listGenerator.generateList(new File(config.getString("irc.listRoot")));
        listGenerator.printStatistics();
        System.out.println("done");
    }
}
