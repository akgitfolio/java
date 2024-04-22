package git.folio;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WebcamSnapshotTaker extends JFrame {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private JButton captureButton;

    public WebcamSnapshotTaker() {
        super("Webcam Snapshot Taker");

        // Initialize webcam
        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        // Create webcam panel
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setDisplayDebugInfo(true);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(true);

        // Create capture button
        captureButton = new JButton("Capture Snapshot");
        captureButton.addActionListener(this::captureSnapshot);

        // Set up the frame
        setLayout(new BorderLayout());
        add(webcamPanel, BorderLayout.CENTER);
        add(captureButton, BorderLayout.SOUTH);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void captureSnapshot(ActionEvent e) {
        BufferedImage image = webcam.getImage();
        try {
            File outputFile = new File("snapshot_" + System.currentTimeMillis() + ".png");
            ImageIO.write(image, "PNG", outputFile);
            JOptionPane.showMessageDialog(this, "Snapshot saved as " + outputFile.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving snapshot: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WebcamSnapshotTaker frame = new WebcamSnapshotTaker();
            frame.setVisible(true);
        });
    }
}
