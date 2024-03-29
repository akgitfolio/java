package git.folio;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MessageQueue {
    private static final int MAX_MESSAGES = 100;
    private static final int MESSAGE_SIZE = 256;
    private static final String QUEUE_FILE = "message_queue.dat";
    private static final int HEADER_SIZE = 8; // 4 bytes for write position, 4 bytes for read position

    private FileChannel channel;
    private MappedByteBuffer buffer;

    public MessageQueue() throws IOException {
        Path path = Paths.get(QUEUE_FILE);
        channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, HEADER_SIZE + (MAX_MESSAGES * MESSAGE_SIZE));

        // Initialize write and read positions if the file is new
        if (channel.size() == 0) {
            buffer.putInt(0, 0); // Write position
            buffer.putInt(4, 0); // Read position
        }
    }

    public void send(String message) throws IOException {
        byte[] messageBytes = message.getBytes();
        if (messageBytes.length > MESSAGE_SIZE - 4) {
            throw new IllegalArgumentException("Message too long");
        }

        int writePosition = buffer.getInt(0);
        int readPosition = buffer.getInt(4);

        if ((writePosition + 1) % MAX_MESSAGES == readPosition) {
            throw new IOException("Queue is full");
        }

        int offset = HEADER_SIZE + (writePosition * MESSAGE_SIZE);
        buffer.putInt(offset, messageBytes.length);
        buffer.position(offset + 4);
        buffer.put(messageBytes);

        buffer.putInt(0, (writePosition + 1) % MAX_MESSAGES);
        buffer.force();
    }

    public String receive() throws IOException {
        int writePosition = buffer.getInt(0);
        int readPosition = buffer.getInt(4);

        if (readPosition == writePosition) {
            return null; // No message available
        }

        int offset = HEADER_SIZE + (readPosition * MESSAGE_SIZE);
        int messageLength = buffer.getInt(offset);

        if (messageLength <= 0 || messageLength > MESSAGE_SIZE - 4) {
            // Invalid message length, move to next position
            buffer.putInt(4, (readPosition + 1) % MAX_MESSAGES);
            buffer.force();
            return null;
        }

        byte[] messageBytes = new byte[messageLength];
        buffer.position(offset + 4);

        // Check if there's enough data to read
        if (buffer.remaining() < messageLength) {
            return null; // Not enough data available
        }

        buffer.get(messageBytes);

        buffer.putInt(4, (readPosition + 1) % MAX_MESSAGES);
        buffer.force();

        return new String(messageBytes);
    }

    public void close() throws IOException {
        channel.close();
    }
}