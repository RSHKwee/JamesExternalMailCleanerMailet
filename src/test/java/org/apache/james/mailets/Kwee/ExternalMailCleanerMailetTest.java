package org.apache.james.mailets.Kwee;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.MailetContext;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class ExternalMailCleanerMailetTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalMailCleanerMailet.class);

  private ExternalMailCleanerMailet mailet;
  private MailetContext mailetContext;
  private FakeMailetConfig mailetConfig;

  private GreenMail greenMail;
  private String user = "test@localhost";
  private String userid = "user";
  private String password = "password";
  private String folderNameArchive = "Archived";

  @BeforeClass
  public void startMailServer() {
    ServerSetup imap = new ServerSetup(3143, null, "imap");
    ServerSetup smtp = new ServerSetup(3025, null, "smtp");
    greenMail = new GreenMail(new ServerSetup[] { imap, smtp });
    // Start IMAP (3143) + SMTP (3025)
    // greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP);
    greenMail.start();

    // Create testuser
    greenMail.setUser(user, password);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    mailet = new ExternalMailCleanerMailet();
    mailetContext = mock(MailetContext.class);
    //@formatter:off
    mailetConfig = FakeMailetConfig.builder()
        .mailetName("ExternMailCleaner")
        .mailetContext(mailetContext)
        .setProperty("jamesConfDir", "")
        .setProperty("dryRun", "false")
        .setProperty("defaultDaysOld", "30")
        .setProperty("archiveFolder", folderNameArchive)
        .setProperty("deleteOriginals", "false")
        .build();
    //@formatter:on

    mailet = new ExternalMailCleanerMailet();
    mailetContext = mock(MailetContext.class);

  }

  @BeforeMethod
  public void resetMailServer() {
    try {
      greenMail.purgeEmailFromAllMailboxes();
    } catch (FolderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } // Maak mailboxen leeg voor elke test
  }

  @AfterClass
  public void stopMailServer() {
    greenMail.stop();
  }

  @Test
  public void testInitShouldSetDefaultValues() throws MessagingException {
    mailet.init(mailetConfig);
    assertEquals(mailet.getInitParameter("jamesConfDir"), "");
    assertEquals(mailet.getInitParameter("defaultDaysOld"), "30");
    assertEquals(mailet.getInitParameter("dryRun"), "false");
  }

  @Test
  public void testCleanImapAccountDryRun() throws Exception {
    mailet.init(mailetConfig);
    try (Store store = greenMail.getImap().createStore()) {
      String folderName = "INBOX";
      ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

      GreenMailUser guser = greenMail.setUser(user, userid, password);
      MailFolder inbox = imapHostManager.getFolder(guser, folderName);
      int count = fillInbox(inbox);

      mailet.setInitParameter("dryRun", "true");
      mailet.service(null);
      Assert.assertEquals(inbox.getMessageCount(), count, "There should be " + count + "  mails present.");

      MailFolder archive = imapHostManager.getFolder(guser, folderNameArchive);
      Assert.assertEquals(archive.getMessageCount(), 0, "There should be 0  mails present.");
      listFolders();
    }
  }

  @Test
  public void testCleanImapAccountRun() throws Exception {
    mailet.init(mailetConfig);
    try (Store store = greenMail.getImap().createStore()) {
      String folderName = "INBOX";
      ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

      GreenMailUser guser = greenMail.setUser(user, userid, password);
      MailFolder inbox = imapHostManager.getFolder(guser, folderName);
      int count = fillInbox(inbox);

      mailet.setInitParameter("dryRun", "false");
      mailet.service(null);
      Assert.assertEquals(inbox.getMessageCount(), count, "There should be " + count + "  mails present.");

      MailFolder archive = imapHostManager.getFolder(guser, folderNameArchive);
      Assert.assertEquals(archive.getMessageCount(), 2, "There should be 2  mails present.");
      listFolders();
    }
  }

  @Test
  public void testCleanImapAccountRunDelInb() throws Exception {
    mailetConfig = FakeMailetConfig.builder().mailetName("ExternMailCleaner").mailetContext(mailetContext)
        .setProperty("jamesConfDir", "").setProperty("dryRun", "false").setProperty("defaultDaysOld", "30")
        .setProperty("archiveFolder", folderNameArchive).setProperty("deleteOriginals", "true").build();
    mailet.init(mailetConfig);

    try (Store store = greenMail.getImap().createStore()) {
      String folderName = "INBOX";
      ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

      GreenMailUser guser = greenMail.setUser(user, userid, password);
      MailFolder inbox = imapHostManager.getFolder(guser, folderName);
      int count = fillInbox(inbox);

      mailet.service(null);
      listFolders();

      Assert.assertEquals(inbox.getMessageCount(), count, "There should be " + count + "  mails present.");

      MailFolder archive = imapHostManager.getFolder(guser, folderNameArchive);
      Assert.assertEquals(archive.getMessageCount(), 2, "There should be 2  mails present.");

      MailFolder bin = imapHostManager.getFolder(guser, "trash");
      Assert.assertEquals(bin.getMessageCount(), 2, "There should be 2 mails present.");
    }
  }

  /**
   * 
   * @throws Exception
   */
  @Test
  public void testEmailDelivery() throws Exception {
    // Verstuur testmail via SMTP
    GreenMailUtil.sendTextEmail(user, "sender@example.com", "Test Subject", "Test Body",
        greenMail.getSmtp().getServerSetup());

    // Verifieer ontvangst via IMAP
    try (Store store = greenMail.getImap().createStore()) {
      store.connect(user, password);
      Folder inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_ONLY);

      Assert.assertEquals(inbox.getMessageCount(), 1, "Er zou 1 e-mail moeten zijn");

      Message message = inbox.getMessage(1);
      Assert.assertEquals(message.getSubject(), "Test Subject");
    }
  }

  @Test
  public void testFillingInbox() throws Exception {
    try (Store store = greenMail.getImap().createStore()) {
      String folderName = "INBOX";
      ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

      GreenMailUser guser = greenMail.setUser(user, userid, password);
      MailFolder inbox = imapHostManager.getFolder(guser, folderName);
      int count = fillInbox(inbox);

      Assert.assertEquals(inbox.getMessageCount(), count, "There should be " + count + "  mails present.");
    }
  }

  // Private methods
  private void listFolders() {
    // Haal de ImapHostManager op
    ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

    // Haal de root folder op
    GreenMailUser guser = greenMail.setUser(user, userid, password);
    try {
      // Alle mappen op hetzelfde niveau als INBOX (in de root)
      String pattern = "*";
      Collection<MailFolder> peerFolders = imapHostManager.listMailboxes(guser, pattern);

      LOGGER.info("Folders voor " + guser.getEmail() + ":");
      peerFolders.forEach(fold -> {
        try {
          listFolders(imapHostManager, guser, fold.getFullName(), 0);
        } catch (FolderException e) {
          // Do nothing
        }
      });
    } catch (FolderException e) {
      // Do nothing
    }
  }

  private static void listFolders(ImapHostManager imapHostManager, GreenMailUser user, String folderName, int level)
      throws FolderException {
    MailFolder folder = imapHostManager.getFolder(user, folderName);
    LOGGER.info(
        "  ".repeat(level) + folder.getName() + " (" + folder.getFullName() + ") #msg: " + folder.getMessageCount());

    // Haal subfolders op via listMailboxes
    String pattern = folder.getFullName() + "%*";
    Collection<MailFolder> subFolders = imapHostManager.listMailboxes(user, pattern);

    for (MailFolder subFolder : subFolders) {
      if (!subFolder.getFullName().equals(folder.getFullName())) {
        listFolders(imapHostManager, user, subFolder.getFullName(), level + 1);
      }
    }
  }

  private int fillInbox(MailFolder inbox) {
    fillMailbox(inbox, 30, 0); // 1
    fillMailbox(inbox, 35, 2); // 2
    fillMailbox(inbox, 30, 1); // 3
    fillMailbox(inbox, 30, 4); // 4
    fillMailbox(inbox, 30, 6); // 5
    fillMailbox(inbox, 30, 2); // 6
    fillMailbox(inbox, 40, 1); // 7
    return 7;
  }

  /**
   * 
   * @param folder
   * @param DaysInPast
   * @param receivedDay
   */
  private void fillMailbox(MailFolder folder, int DaysInPast, int receivedDay) {
    MimeMessage msg;
    try {
      msg = new MimeMessage(createMailRandomSentDate(DaysInPast));
      Date receivedDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DaysInPast - receivedDay));
      folder.appendMessage(msg, new Flags(), receivedDate);
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param DaysInPast
   * @return
   */
  private MimeMessage createMailRandomSentDate(int DaysInPast) {
    Properties props = new Properties();
    Session session = Session.getInstance(props);

    MimeMessage message = new MimeMessage(session);
    try {
      message.setFrom(new InternetAddress("sender@example.com"));
      Date randomDate = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DaysInPast));

      message.addRecipient(Message.RecipientType.TO, new InternetAddress("receiver@example.com"));
      message.setSubject("Test met aangepaste datum");
      message.setText("Inhoud van de e-mail " + randomDate.toString());

      message.setSentDate(randomDate);
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return message;
  }
}
