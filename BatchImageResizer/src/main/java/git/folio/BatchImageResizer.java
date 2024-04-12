package git.folio;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BatchImageResizer {

    private static final int TARGET_WIDTH = 300;
    private static final int TARGET_HEIGHT = 200;
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        String inputDir = "./images";
        String outputDir = "./output";

        File inputFolder = new File(inputDir);
        File outputFolder = new File(outputDir);

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        File[] files = inputFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (isImageFile(file)) {
                    executor.submit(() -> {
                        try {
                            resizeImage(file, outputFolder);
                        } catch (IOException e) {
                            System.err.println("Error processing file: " + file.getName());
                            e.printStackTrace();
                        }
                    });
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Executor interrupted: " + e.getMessage());
        }

        System.out.println("Batch image resizing completed.");
    }

    private static void resizeImage(File inputFile, File outputFolder) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);

        BufferedImage resizedImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        g2d.dispose();

        String fileName = inputFile.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        File outputFile = new File(outputFolder, "resized_" + fileName);

        ImageIO.write(resizedImage, fileExtension, outputFile);
        System.out.println("Resized: " + fileName);
    }

    private static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
    }
}