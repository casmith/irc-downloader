package marvin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ListSearcher {

    private List<String> lines = new ArrayList<>();

    public ListSearcher(File listsDirectory) throws FileNotFoundException {
        File[] files = listsDirectory.listFiles();
        for (File file : files) {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lines.add(line);
            }
        }
        System.out.println("Initialized with " + lines.size() + " lines");
    }

    public List<String> search(final String regex) {
        return lines.stream()
                .filter(s -> s.matches(regex))
                .collect(Collectors.toList());
    }
}
