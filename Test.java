import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) {
        // String m = null;
        // try (FileInputStream fIn = new FileInputStream(new File(args[0]))) {
        //     m = new String(fIn.readAllBytes(), StandardCharsets.UTF_8);
        // } catch (Exception E) {
        //     return;
        // }
        String m = "/*\n@Dhulb \n*/";
        if (m.matches("(?s)/\\*[\\s*]*@Dhulb.*")) {
            System.out.println("match");
        } else {
            System.out.println("no match");
        }
    }
}
