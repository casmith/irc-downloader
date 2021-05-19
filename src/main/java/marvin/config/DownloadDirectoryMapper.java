package marvin.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadDirectoryMapper {
    Map<Pattern, File> regexMappings = new HashMap<>();
    Map<String, File> extensionMappings = new HashMap<>();
    private final String defaultDestination;

    public DownloadDirectoryMapper(String defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

    public void add(String regexOrExtension, File destination) {
        Pattern pattern = Pattern.compile("^/(.*)/$");
        Matcher matcher = pattern.matcher(regexOrExtension);
        if (matcher.matches()) {
            System.out.println("regex: " + matcher.group(1));
            regexMappings.put(Pattern.compile(matcher.group(1)), destination);
        } else {
            System.out.println("extension: " + regexOrExtension);
            extensionMappings.put(regexOrExtension, destination);
        }
    }

    public File map(String fileName) {
        for (Map.Entry<Pattern, File> regexStringEntry : regexMappings.entrySet()) {
            if (regexStringEntry.getKey().matcher(fileName).matches()) {
                return regexStringEntry.getValue();
            }
        }
        for (Map.Entry<String, File> entry : extensionMappings.entrySet()) {
            if (fileName.endsWith("." + entry.getKey())) {
                return entry.getValue();
            }
        }
        return new File(defaultDestination);
    }
}
