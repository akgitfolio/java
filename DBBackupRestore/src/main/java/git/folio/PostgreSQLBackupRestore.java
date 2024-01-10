package git.folio;
import java.io.IOException;

public class PostgreSQLBackupRestore {

    private static final String PG_DUMP_PATH = "/usr/bin/pg_dump";
    private static final String PSQL_PATH = "/usr/bin/psql";

    public static void backup(String host, String port, String user, String database, String backupPath) throws IOException {
        String command = String.format("%s -h %s -p %s -U %s -F c -b -v -f %s %s",
                PG_DUMP_PATH, host, port, user, backupPath, database);
        Process process = Runtime.getRuntime().exec(command);
        // Wait for the process to complete
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void restore(String host, String port, String user, String database, String backupPath) throws IOException {
        String command = String.format("%s -h %s -p %s -U %s -d %s -1 -f %s",
                PSQL_PATH, host, port, user, database, backupPath);
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
            backup("localhost", "5432", "postgres", "mydb", "/backup/mydb_backup.dump");
            restore("localhost", "5432", "postgres", "mydb", "/backup/mydb_backup.dump");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}