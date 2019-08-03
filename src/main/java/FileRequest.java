import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRequest implements Serializable {

    private String nick;
    private String fileName;
    private static Pattern pattern = Pattern.compile("^!([A-Za-z0-1]+) (.*\\.mp3)(.*)$");

    public FileRequest(String nick, String fileName) {
        this.nick = nick;
        this.fileName = fileName;
    }

    public static FileRequest parse(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String nick = matcher.group(1);
            String fileName = matcher.group(2);
            return new FileRequest(nick, fileName);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileRequest that = (FileRequest) o;

        if (nick != null ? !nick.equals(that.nick) : that.nick != null) return false;
        return fileName != null ? fileName.equals(that.fileName) : that.fileName == null;
    }

    @Override
    public int hashCode() {
        int result = nick != null ? nick.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }
}
