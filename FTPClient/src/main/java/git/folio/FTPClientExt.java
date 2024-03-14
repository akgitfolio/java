package git.folio;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FTPClientExt {

    private static final String SERVER = "ftp.example.com";
    private static final int PORT = 21;
    private static final String USER = "username";
    private static final String PASS = "password";

    public static void main(String[] args) {
        FTPClient ftpClient = new FTPClient();
        try {
            // Connect and login to the server
            ftpClient.connect(SERVER, PORT);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Connection failed. Server reply code: " + replyCode);
                return;
            }
            boolean success = ftpClient.login(USER, PASS);
            if (!success) {
                System.out.println("Could not login to the server");
                return;
            }

            // Set file transfer mode to binary
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            // Upload a file
            String localFilePath = "path/to/local/file.txt";
            String remoteFilePath = "/remote/directory/file.txt";
            try (InputStream inputStream = new FileInputStream(localFilePath)) {
                System.out.println("Uploading file...");
                boolean uploaded = ftpClient.storeFile(remoteFilePath, inputStream);
                if (uploaded) {
                    System.out.println("File uploaded successfully.");
                } else {
                    System.out.println("Failed to upload the file.");
                }
            }

            // Download a file
            String downloadFilePath = "path/to/download/file.txt";
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFilePath))) {
                System.out.println("Downloading file...");
                boolean downloaded = ftpClient.retrieveFile(remoteFilePath, outputStream);
                if (downloaded) {
                    System.out.println("File downloaded successfully.");
                } else {
                    System.out.println("Failed to download the file.");
                }
            }

            // List files in a directory
            String remoteDirectory = "/remote/directory";
            System.out.println("Listing files in directory: " + remoteDirectory);
            String[] files = ftpClient.listNames(remoteDirectory);
            if (files != null && files.length > 0) {
                for (String file : files) {
                    System.out.println(file);
                }
            } else {
                System.out.println("No files found in the directory.");
            }

            // Delete a file
            boolean deleted = ftpClient.deleteFile(remoteFilePath);
            if (deleted) {
                System.out.println("File deleted successfully.");
            } else {
                System.out.println("Failed to delete the file.");
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}