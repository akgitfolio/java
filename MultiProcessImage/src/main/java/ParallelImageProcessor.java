import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;


public class ParallelImageProcessor {
    private static final int NUM_PROCESSES = Runtime.getRuntime().availableProcessors();
    private static final String INPUT_DIR = "images";
    private static final String OUTPUT_DIR = "output";

    public static void main(String[] args) throws IOException, InterruptedException {
        File inputFolder = new File(INPUT_DIR);
        File outputFolder = new File(OUTPUT_DIR);
        outputFolder.mkdirs();

        File[] imageFiles = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found in the input directory.");
            return;
        }

        List<Process> processes = new ArrayList<>();
        int imagesPerProcess = imageFiles.length / NUM_PROCESSES;

        String classpath = System.getProperty("java.class.path");

        for (int i = 0; i < NUM_PROCESSES; i++) {
            int startIndex = i * imagesPerProcess;
            int endIndex = (i == NUM_PROCESSES - 1) ? imageFiles.length : (i + 1) * imagesPerProcess;

            ProcessBuilder pb = new ProcessBuilder("java",
                    "-cp", classpath,
                    "ImageProcessorWorker",
                    INPUT_DIR, OUTPUT_DIR,
                    String.valueOf(startIndex),
                    String.valueOf(endIndex));
            pb.inheritIO(); // Redirect worker process I/O to parent process
            Process process = pb.start();
            processes.add(process);
        }

        // Wait for all processes to complete
        for (Process process : processes) {
            process.waitFor();
        }

        System.out.println("All image processing completed.");
    }
}
class ImageProcessorWorker {
    private static final int TARGET_WIDTH = 300;
    private static final int TARGET_HEIGHT = 200;

    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];
        int startIndex = Integer.parseInt(args[2]);
        int endIndex = Integer.parseInt(args[3]);

        File inputFolder = new File(inputDir);
        File[] imageFiles = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        for (int i = startIndex; i < endIndex; i++) {
            processImage(imageFiles[i], outputDir);
        }
    }

    private static void processImage(File inputFile, String outputDir) {
        try {
            System.out.println("Processing: " + inputFile.getName());

            // Read the image
            BufferedImage originalImage = ImageIO.read(inputFile);

            // Resize the image
            BufferedImage resizedImage = resizeImage(originalImage);

            // Convert to grayscale
            BufferedImage grayscaleImage = convertToGrayscale(resizedImage);

            // Apply edge detection
            BufferedImage edgeDetectedImage = applyEdgeDetection(grayscaleImage);

            // Save the processed image
            String outputFileName = "processed_" + inputFile.getName();
            File outputFile = new File(outputDir, outputFileName);
            ImageIO.write(edgeDetectedImage, "png", outputFile);

            System.out.println("Finished processing: " + inputFile.getName());
        } catch (IOException e) {
            System.err.println("Error processing file: " + inputFile.getName());
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage) {
        Image tmp = originalImage.getScaledInstance(TARGET_WIDTH, TARGET_HEIGHT, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    private static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayscaleImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayscaleImage;
    }

    private static BufferedImage applyEdgeDetection(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelX = 0, pixelY = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = image.getRGB(x + i, y + j) & 0xFF;
                        pixelX += pixel * sobelX[i + 1][j + 1];
                        pixelY += pixel * sobelY[i + 1][j + 1];
                    }
                }
                int magnitude = (int) Math.sqrt(pixelX * pixelX + pixelY * pixelY);
                magnitude = Math.min(255, Math.max(0, magnitude));
                edgeImage.setRGB(x, y, (magnitude << 16) | (magnitude << 8) | magnitude);
            }
        }
        return edgeImage;
    }
}