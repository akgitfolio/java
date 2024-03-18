package git.folio;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class DirectoryWatcher {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;

    public DirectoryWatcher(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void watchDirectory() {
        try {
            while (true) {
                WatchKey key = watcher.take();
                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    System.out.format("%s: %s\n", event.kind().name(), child);

                    if (recursive && (kind == ENTRY_CREATE)) {
                        try {
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readable
                        }
                    }

                    if (kind == ENTRY_CREATE) {
                        handleNewFile(child);
                    } else if (kind == ENTRY_MODIFY) {
                        handleModifiedFile(child);
                    } else if (kind == ENTRY_DELETE) {
                        handleDeletedFile(child);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleNewFile(Path file) {
        System.out.println("Handling new file: " + file);
        // Add logic to handle new file
    }

    private void handleModifiedFile(Path file) {
        System.out.println("Handling modified file: " + file);
        // Add logic to handle modified file
    }

    private void handleDeletedFile(Path file) {
        System.out.println("Handling deleted file: " + file);
        // Add logic to handle deleted file
    }

    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("./");
        boolean recursive = true;
        DirectoryWatcher watcher = new DirectoryWatcher(dir, recursive);
        watcher.watchDirectory();
    }
}