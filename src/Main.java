import java.io.File;
import java.io.IOException;
public class Main{

     public static void main(String[] args) {
        try {
            // Setup files
            File originalFile = new File("logo128.png"); 
            String secretMessage = "xpto"; // Example string

            // 1. Hide the message
            System.out.println("Embedding message...");
            SteganographyHelper.embedMessage(originalFile, secretMessage);

            // 2. Read the message back from the generated file
            System.out.println("Extracting message from generated file...");
            File stegoFile = new File("logo128_X.png");
            System.out.println(SteganographyHelper.extractMessage(stegoFile));

        } catch (IOException e) {
            System.err.println("Process error: " + e.getMessage());
        }
    }

}
