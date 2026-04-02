
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A utility class to hide and retrieve a fixed 16-character string 
 * within the pixels of a PNG image.
 * @author rafaone@proton.me
 */
public class SteganographyHelper {

    private static final int FIXED_LENGTH = 16; // 16 bytes = 128 bits

   
    /**
     * Embeds a string into a PNG file.
     * Creates a new file with the suffix '_X.png'.
     */
    public static void embedMessage(File pngFile, String message) throws IOException {
        BufferedImage image = ImageIO.read(pngFile);
        if (image == null) throw new IOException("Could not read image file.");

        // Ensure the string is exactly 16 bytes
        byte[] msgBytes = formatToFixedLength(message);
        boolean[] bits = bytesToBits(msgBytes);

        int bitIndex = 0;
        int width = image.getWidth();
        int height = image.getHeight();

        // Loop through pixels to inject bits into LSB of RGB channels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= bits.length) break;

                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >> 8)  & 0xFF;
                int blue  =  argb        & 0xFF;

                // Red Channel LSB
                if (bitIndex < bits.length) {
                    red = (red & 0xFE) | (bits[bitIndex++] ? 1 : 0);
                }
                // Green Channel LSB
                if (bitIndex < bits.length) {
                    green = (green & 0xFE) | (bits[bitIndex++] ? 1 : 0);
                }
                // Blue Channel LSB
                if (bitIndex < bits.length) {
                    blue = (blue & 0xFE) | (bits[bitIndex++] ? 1 : 0);
                }

                int newArgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newArgb);
            }
            if (bitIndex >= bits.length) break;
        }

        // Save the result
        String fileName = pngFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        File outputFile = new File(pngFile.getParent(), baseName + "_X.png");
        
        ImageIO.write(image, "png", outputFile);
        System.out.println("Stego-image created at: " + outputFile.getAbsolutePath());
    }

    /**
     * Extracts the hidden 16-character string from a PNG file.
     */
    public static String extractMessage(File pngFile) throws IOException {
      
        BufferedImage image = ImageIO.read(pngFile);
        if (image == null) throw new IOException("Could not read image file.");

        int totalBitsNeeded = FIXED_LENGTH * 8;
        boolean[] bits = new boolean[totalBitsNeeded];
        int bitIndex = 0;

        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (bitIndex >= totalBitsNeeded) break outer;

                int argb = image.getRGB(x, y);
                
                // Extract LSB from Red, Green, and Blue
                bits[bitIndex++] = (argb & 0x00010000) != 0;
                if (bitIndex >= totalBitsNeeded) break outer;

                bits[bitIndex++] = (argb & 0x00000100) != 0;
                if (bitIndex >= totalBitsNeeded) break outer;

                bits[bitIndex++] = (argb & 0x00000001) != 0;
            }
        }

        byte[] msgBytes = bitsToBytes(bits);
        String decoded = new String(msgBytes, StandardCharsets.UTF_8);
        
        return decoded;
        
      
    }

    // --- HELPER METHODS ---

    private static byte[] formatToFixedLength(String msg) {
        byte[] original = msg.getBytes(StandardCharsets.UTF_8);
        byte[] fixed = new byte[FIXED_LENGTH];
        Arrays.fill(fixed, (byte) 32); // Fill with spaces (ASCII 32)
        
        int lengthToCopy = Math.min(original.length, FIXED_LENGTH);
        System.arraycopy(original, 0, fixed, 0, lengthToCopy);
        return fixed;
    }

    private static boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j] = (bytes[i] & (1 << (7 - j))) != 0;
            }
        }
        return bits;
    }

    private static byte[] bitsToBytes(boolean[] bits) {
        byte[] bytes = new byte[bits.length / 8];
        for (int i = 0; i < bytes.length; i++) {
            int val = 0;
            for (int j = 0; j < 8; j++) {
                if (bits[i * 8 + j]) {
                    val |= (1 << (7 - j));
                }
            }
            bytes[i] = (byte) val;
        }
        return bytes;
    }
}