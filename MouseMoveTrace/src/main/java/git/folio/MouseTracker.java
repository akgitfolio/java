package git.folio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MouseTracker extends JFrame {
    private JTextArea logArea;

    public MouseTracker() {
        setTitle("Mouse Movement Tracker");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a panel to track mouse movements
        JPanel trackingPanel = new JPanel();
        trackingPanel.setBackground(Color.WHITE);
        trackingPanel.setPreferredSize(new Dimension(500, 300));

        // Create a text area for logging
        logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // Add mouse motion listener to the tracking panel
        trackingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                logMouseMovement(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                logMouseMovement(e);
            }
        });

        // Add components to the frame
        add(trackingPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void logMouseMovement(MouseEvent e) {
        String message = String.format("Mouse moved to: (%d, %d)", e.getX(), e.getY());
        System.out.println(message); // Log to console
        logArea.append(message + "\n"); // Log to text area
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Scroll to bottom
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MouseTracker tracker = new MouseTracker();
            tracker.setVisible(true);
        });
    }
}
