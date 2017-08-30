import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRequest {

    private String nick;
    private String fileName;
    private static Pattern pattern = Pattern.compile("^!([A-Za-z0-1]+) (.*\\.mp3)(.*)$");

    public FileRequest(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            this.nick = matcher.group(1);
            this.fileName = matcher.group(2);
        } else {
            throw new ParseException("Wrong pattern: " + line);
        }
    }

    public String getNick() {
        return nick;
    }

    public String getFileName() {
        return fileName;
    }
}
