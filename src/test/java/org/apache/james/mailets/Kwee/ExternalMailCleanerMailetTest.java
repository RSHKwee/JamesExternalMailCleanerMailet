package org.apache.james.mailets.Kwee;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.mailet.MailetContext;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserManager;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class ExternalMailCleanerMailetTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalMailCleanerMailet.class);

  private ExternalMailCleanerMailet mailet;
  private MailetContext mailetContext;
  private FakeMailetConfig mailetConfig;

  private GreenMail greenMail;
  private GreenMailUser guser;
  private String user = "test@localhost";
  private String userid = "user";
  private String c_userid = "user";
  private String password = "password";
  private String folderNameArchive = "Archived";
  private String c_folderNameArchive = "Archived";

  @BeforeMethod
  public void setUp() throws Exception {
    ServerSetup imap = new ServerSetup(3143, null, "imap");
    ServerSetup smtp = new ServerSetup(3025, null, "smtp");
    greenMail = new GreenMail(new ServerSetup[] { imap, smtp });
    // Start IMAP (3143) + SMTP (3025)
    // greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP);
    greenMail.start();

    // Create testuser
    // user = System.currentTimeMillis() + "_" + c_user;
    // userid = System.currentTimeMillis() + "_" + c_userid;
    // folderNameArchive = System.currentTimeMillis() + "_" + c_folderNameArchive;

    guser = greenMail.setUser(user, password);

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

  @AfterMethod
  public void tearDown() {
    if (greenMail != null) {
      // Clear all users before next test
      clearAllUsers(greenMail);
      greenMail.reset();
      greenMail.stop();
      greenMail = null;
    }
  }

  @AfterClass
  public void stopMailServer() {
    if (greenMail != null) {
      greenMail.reset();
      greenMail.stop();
    }
  }

  @Test
  public void testInitShouldSetDefaultValues() throws MessagingException, jakarta.mail.MessagingException {
    synchronized (ExternalMailCleanerMailetTest.class) {
      // Reset state before test
      try {
        greenMail.purgeEmailFromAllMailboxes();
      } catch (FolderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      mailet.init(mailetConfig);
      assertEquals(mailet.getInitParameter("jamesConfDir"), "");
      assertEquals(mailet.getInitParameter("defaultDaysOld"), "30");
      assertEquals(mailet.getInitParameter("dryRun"), "false");
    }
  }

  @Test
  public void testCleanImapAccountDryRun() throws Exception {
    synchronized (ExternalMailCleanerMailetTest.class) {
      // Reset state before test
      try {
        greenMail.purgeEmailFromAllMailboxes();
      } catch (FolderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      mailet.init(mailetConfig);
      // Use Jakarta session instead of direct Store creation
      Session session = createJakartaSession();
      try (Store store = session.getStore("imap")) {
        store.connect("localhost", greenMail.getImap().getPort(), user, password);
        String folderName = "INBOX";
        ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

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
  }

  @Test
  public void testCleanImapAccountRun() throws Exception {
    synchronized (ExternalMailCleanerMailetTest.class) {
      // Reset state before test
      try {
        greenMail.purgeEmailFromAllMailboxes();
      } catch (FolderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      mailet.init(mailetConfig);
      Session session = createJakartaSession();
      try (Store store = session.getStore("imap")) {
        String folderName = "INBOX";
        ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

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
  }

  @Test
  public void testCleanImapAccountRunDelInb() throws Exception {
    synchronized (ExternalMailCleanerMailetTest.class) {
      // Reset state before test
      try {
        greenMail.purgeEmailFromAllMailboxes();
      } catch (FolderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      mailetConfig = FakeMailetConfig.builder().mailetName("ExternMailCleaner").mailetContext(mailetContext)
          .setProperty("jamesConfDir", "").setProperty("dryRun", "false").setProperty("defaultDaysOld", "30")
          .setProperty("archiveFolder", folderNameArchive).setProperty("deleteOriginals", "true").build();
      mailet.init(mailetConfig);

      Session session = createJakartaSession();
      try (Store store = session.getStore("imap")) {
        String folderName = "INBOX";
        ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

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
  }

  /**
   * 
   * @throws Exception
   */
  @Test
  public void testEmailDelivery() throws Exception {
    synchronized (ExternalMailCleanerMailetTest.class) {
      // Reset state before test
      try {
        greenMail.purgeEmailFromAllMailboxes();
      } catch (FolderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Verstuur testmail via SMTP
      GreenMailUtil.sendTextEmail(user, "sender@example.com", "Test Subject", "Test Body",
          greenMail.getSmtp().getServerSetup());

      // Verifieer ontvangst via IMAP
      Session session = createJakartaSession();
      try (Store store = session.getStore("imap")) {
        store.connect(user, password);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Assert.assertEquals(inbox.getMessageCount(), 1, "Er zou 1 e-mail moeten zijn");

        Message message = inbox.getMessage(1);
        Assert.assertEquals(message.getSubject(), "Test Subject");
      }
    }
  }

  @Test
  public void testFillingInbox() throws Exception {
    synchronized (ExternalMailCleanerMailetTest.class) {
      // Reset state before test
      try {
        greenMail.purgeEmailFromAllMailboxes();
      } catch (FolderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Session session = createJakartaSession();
      try (Store store = session.getStore("imap")) {
        String folderName = "INBOX";
        ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

        MailFolder inbox = imapHostManager.getFolder(guser, folderName);
        int count = fillInbox(inbox);

        Assert.assertEquals(inbox.getMessageCount(), count, "There should be " + count + "  mails present.");
      }
    }
  }

  // Private methods
  private void safeSetUser(String email, String login, String password) {
    UserManager userManager = greenMail.getManagers().getUserManager();

    try {
      GreenMailUser existingUser = userManager.getUser(email);
      userManager.deleteUser(existingUser);
      System.out.println("Deleted existing user: " + email);
    } catch (Exception e) {
      // User doesn't exist - this is expected for first creation
    }

    // Create new user
    greenMail.setUser(email, login, password);
    System.out.println("Created user: " + email);
  }

  private void clearAllUsers(GreenMail greenMail) {
    try {
      UserManager userManager = greenMail.getManagers().getUserManager();
      Collection<GreenMailUser> users = userManager.listUser();

      for (GreenMailUser user : users) {
        userManager.deleteUser(user);
        System.out.println("Cleaned up user: " + user.getEmail());
      }
    } catch (Exception e) {

    }
  }

  private Session createJakartaSession() {
    java.util.Properties props = new java.util.Properties();
    props.put("mail.imap.host", "localhost");
    props.put("mail.imap.port", greenMail.getImap().getPort());
    props.put("mail.imap.auth", "false");
    props.put("mail.imap.starttls.enable", "false");
    return Session.getInstance(props);
  }

  private void listFolders() {
    // Haal de ImapHostManager op
    ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();

    // Haal de root folder op
    // GreenMailUser guser = greenMail.setUser(user, userid, password);
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
