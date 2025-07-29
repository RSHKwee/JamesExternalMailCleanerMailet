package sandbox;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

public class GmailIMAPDownloader {

  public static void main(String[] args) {
    // Configuratiegegevens
    String host = "imap.gmail.com";
    String username = "cym.kwee@gmail.com";
    String password = "ycgapizqflbgaibe";
    int port = 993;
    int maxEmails = 10; // Maximum aantal te downloaden emails

    try {
      // 1. Eigenschappen instellen voor IMAPS
      Properties properties = new Properties();
      properties.put("mail.imap.host", host);
      properties.put("mail.imap.port", port);
      properties.put("mail.imap.ssl.enable", "true");
      properties.put("mail.imap.auth", "true");
      properties.put("mail.imap.starttls.enable", "true");
      properties.put("mail.imap.ssl.protocols", "TLSv1.2");

      // 2. Sessie maken
      Session session = Session.getInstance(properties);
      session.setDebug(false); // Debug output

      // 3. Verbinding maken met IMAP store
      try (Store store = session.getStore("imap")) {
        store.connect(host, port, username, password);

        // 4. Inbox openen
        try (Folder inbox = store.getFolder("INBOX")) {
          inbox.open(Folder.READ_ONLY);

          // 5. Berichten ophalen (nieuwste eerst)
          Message[] messages = inbox.getMessages();
          System.out.println("Totaal aantal berichten: " + messages.length);

          // Beperk het aantal te verwerken berichten
          int count = Math.min(messages.length, maxEmails);

          // 6. Berichten verwerken (van nieuw naar oud)
          for (int i = messages.length - 1; i >= messages.length - count; i--) {
            Message message = messages[i];
            printMessageDetails(message, i);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void printMessageDetails(Message message, int i) throws Exception {
    System.out.println("\n" + i + "---------------------------------");
    System.out.println("Onderwerp: " + message.getSubject());
    System.out.println("Van: " + InternetAddress.toString(message.getFrom()));
    System.out.println("Datum: " + message.getSentDate());

    // Markeringen controleren
    Flags flags = message.getFlags();
    System.out.println("Gelezen: " + flags.contains(Flags.Flag.SEEN));

    // Inhoud ophalen
    Object content = message.getContent();
    if (content instanceof String) {
      System.out.println("Inhoud:\n" + content);
    } else if (content instanceof Multipart) {
      System.out.println("Bericht met meerdere delen:");
      Multipart multipart = (Multipart) content;
      for (int j = 0; j < multipart.getCount(); j++) {
        BodyPart bodyPart = multipart.getBodyPart(j);
        if (bodyPart.getContentType().contains("text/plain")) {
          System.out.println("Tekstgedeelte:\n" + bodyPart.getContent());
        } else if (bodyPart.getContentType().contains("text/html")) {
          System.out.println("HTML-gedeelte:\n" + bodyPart.getContent());
        }
      }
    }
  }
}