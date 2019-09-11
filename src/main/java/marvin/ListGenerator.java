package marvin;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ListGenerator {
    private final FileWriter fileWriter;
    private final String nick;
    private long bytes = 0;
    private long count = 0;

    public ListGenerator(String nick, FileWriter fileWriter) {
        this.nick = nick;
        this.fileWriter = fileWriter;
    }

    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        String nick = config.getString("irc.nick");
        try {
            FileWriter fileWriter = new FileWriter(MessageFormat.format("{0}-Default({1})-MB.txt", nick, LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
            ListGenerator listGenerator = new ListGenerator(nick, fileWriter);
            listGenerator.listFiles(new File(config.getString("irc.listRoot")));
            listGenerator.printStatistics();
            System.out.println("done");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void listFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles(ListGenerator::filterFiles);
            if (files.length > 0) {
                writeLine();
                writeLine("=============================================");
                writeLine(file);
                writeLine("=============================================");
                writeFiles(files);
            }

            File[] dirs = file.listFiles(File::isDirectory);
            if (dirs.length > 0) {
                Arrays.stream(dirs).forEach(this::listFiles);
            }
        }
    }

    public void writeFiles(File[] files) {
        Arrays.stream(files).forEach(file1 -> {
            bytes += file1.length();
            count++;
            writeLine("!" + nick + " " + file1);
        });
    }

    public void writeLine(String line) {
        try {
            fileWriter.write(line + "\n");
            System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine() {
        writeLine("");
    }

    public void writeLine(Object obj) {
        writeLine(obj.toString());
    }
}
