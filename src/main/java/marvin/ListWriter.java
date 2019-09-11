package marvin;

import java.io.FileWriter;
import java.io.IOException;

public class ListWriter implements AutoCloseable {
    private FileWriter fileWriter;

    public ListWriter(FileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    public void writeLine(String line) {
        try {
            fileWriter.write(line + "\n");
        } catch (IOException e) {
            throw new ListGeneratorException("Failed to write line to file", e);
        }
    }

    public void writeLine() {
        writeLine("");
    }

    public void writeLine(Object obj) {
        writeLine(obj.toString());
    }

    @Override
    public void close() throws Exception {
        fileWriter.close();
    }
}
