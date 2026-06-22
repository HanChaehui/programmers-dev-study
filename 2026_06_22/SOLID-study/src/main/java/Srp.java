import java.util.ArrayList;
import java.io.*;

public class Srp {
}

class Journal {
    private ArrayList<String> entries = new ArrayList<>();

    void add(String text) { entries.add(text);}

    String getText() {
        StringBuilder sb = new StringBuilder();
        for (String i : entries) {
            sb.append("- ").append(i).append("\n");
        }
        return sb.toString();
    }
}

class JournalSaver {
    void saveToFile(Journal j, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(j.getText());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    void print(Journal j) {
        System.out.print(j.getText());
    }
}
