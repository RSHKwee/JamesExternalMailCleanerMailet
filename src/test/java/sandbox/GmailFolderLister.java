package sandbox;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

public class GmailFolderLister {

  public static void main(String[] args) {
    // Configuratiegegevens
    String host = "imap.gmail.com";
    String username = "cym.kwee@gmail.com";
    String password = "ycgapizqflbgaibe";
    int port = 993;

    try {
      // Eigenschappen instellen
      Properties properties = new Properties();
      properties.put("mail.imap.host", host);
      properties.put("mail.imap.port", port);
      properties.put("mail.imap.ssl.enable", "true");
      properties.put("mail.imap.auth", "true");
      properties.put("mail.imap.starttls.enable", "true");
      properties.put("mail.store.protocol", "imap");

      // Sessie maken
      Session session = Session.getInstance(properties);
      session.setDebug(false); // Debug output aanzetten

      // Store object maken
      Store store = session.getStore("imap");

      // Connection listener voor debug info
      store.addConnectionListener(new ConnectionListener() {
        @Override
        public void opened(ConnectionEvent e) {
          System.out.println("Verbinding geopend");
        }

        @Override
        public void disconnected(ConnectionEvent e) {
          System.out.println("Verbinding verbroken");
        }

        @Override
        public void closed(ConnectionEvent e) {
          System.out.println("Verbinding gesloten");
        }
      });

      // Verbinding maken
      store.connect(host, port, username, password);

      // Standaardmappen ophalen
      Folder defaultFolder = store.getDefaultFolder();
      System.out.println("\nStandaardmap: " + defaultFolder.getName());

      // Alle mappen ophalen
      Folder[] folders = defaultFolder.list("*");
      System.out.println("\nAlle beschikbare mappen:");
      try {
        // Mappen recursief tonen
        listFolders(folders, 0);
      } catch (Exception e) {
        // e.printStackTrace();
      }
      // Speciale Gmail-mappen
      Folder[] gmailFolders = store.getFolder("[Gmail]").list();
      System.out.println("\nSpeciale Gmail-mappen:");
      for (Folder folder : gmailFolders) {
        System.out.println("- " + folder.getFullName());
      }

      // Verbinding sluiten
      store.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void listFolders(Folder[] folders, int level) throws MessagingException {
    String indent = "  ".repeat(level);
    for (Folder folder : folders) {
      System.out.println(indent + "- " + folder.getFullName() + " (" + folder.getType() + ") " + "Berichten: "
          + folder.getMessageCount());

      // Submappen tonen (indien aanwezig)
      if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
        Folder[] subFolders = folder.list();
        listFolders(subFolders, level + 1);
      }
    }
  }
}