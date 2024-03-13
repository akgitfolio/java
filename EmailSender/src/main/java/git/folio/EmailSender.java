package git.folio;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.File;

public class EmailSender {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String USERNAME = "your-email@gmail.com";
    private static final String PASSWORD = "your-password";

    public static void sendEmailWithAttachments(String to, String subject, String body, String[] attachmentPaths) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Create the message body part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Add attachments
            if (attachmentPaths != null) {
                for (String attachmentPath : attachmentPaths) {
                    addAttachment(multipart, attachmentPath);
                }
            }

            // Set the complete message parts
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully with attachments.");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addAttachment(Multipart multipart, String filePath) throws MessagingException {
        BodyPart attachmentBodyPart = new MimeBodyPart();
        try {
            File file = new File(filePath);
            ((MimeBodyPart) attachmentBodyPart).attachFile(file);
            attachmentBodyPart.setFileName(file.getName());
            multipart.addBodyPart(attachmentBodyPart);
        } catch (Exception e) {
            throw new MessagingException("Error attaching file: " + filePath, e);
        }
    }

    public static void main(String[] args) {
        String to = "recipient@example.com";
        String subject = "Test Email with Attachments";
        String body = "This is a test email with attachments sent from Java.";
        String[] attachments = {
                "/path/to/attachment1.pdf",
                "/path/to/attachment2.jpg"
        };

        sendEmailWithAttachments(to, subject, body, attachments);
    }
}