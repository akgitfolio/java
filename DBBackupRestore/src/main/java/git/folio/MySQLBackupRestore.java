package git.folio;
import java.io.IOException;

public class MySQLBackupRestore {

    private static final String MYSQLDUMP_PATH = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump";
    private static final String MYSQL_PATH = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql";

    public static void backup(String host, String port, String user, String password, String database, String backupPath) throws IOException {
        String command = String.format("%s -h%s --port=%s -u%s --password=%s --add-drop-database -B %s -r %s",
                MYSQLDUMP_PATH, host, port, user, password, database, backupPath);
        Process process = Runtime.getRuntime().exec(command);
        // Wait for the process to complete
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void restore(String host, String port, String user, String password, String database, String backupPath) throws IOException {
        String[] command = new String[]{
                MYSQL_PATH,
                "-h" + host,
                "--port=" + port,
                "-u" + user,
                "-p" + password,
                database,
                "-e",
                "source " + backupPath
        };
        Process process = Runtime.getRuntime().exec(command);
        // Wait for the process to complete
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            backup("localhost", "3306", "root", "password", "mydb", "C:\\backup\\mydb_backup.sql");
            restore("localhost", "3306", "root", "password", "mydb", "C:\\backup\\mydb_backup.sql");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}