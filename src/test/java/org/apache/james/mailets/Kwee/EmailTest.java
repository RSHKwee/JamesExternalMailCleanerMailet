package org.apache.james.mailets.kwee;

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

public class EmailTest {
  private GreenMail greenMail;
  private String user = "test@localhost";
  private String userid = "user";
  private String password = "password";

  @BeforeClass
  public void startMailServer() {
    ServerSetup imap = new ServerSetup(3143, null, "imap");
    ServerSetup smtp = new ServerSetup(3025, null, "smtp");
    greenMail = new GreenMail(new ServerSetup[] { imap, smtp });
    // Start IMAP (3143) + SMTP (3025)
    // greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP);
    greenMail.start();

    // Testgebruiker aanmaken
    greenMail.setUser(user, password);
  }

  @AfterClass
  public void stopMailServer() {
    greenMail.stop();
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

      fillMailbox(inbox, 30, 0); // 1
      fillMailbox(inbox, 35, 2); // 2
      fillMailbox(inbox, 30, 1); // 3
      fillMailbox(inbox, 30, 4); // 4
      fillMailbox(inbox, 30, 6); // 5
      fillMailbox(inbox, 30, 2); // 6
      fillMailbox(inbox, 40, 1); // 7

      Assert.assertEquals(inbox.getMessageCount(), 7, "Er zouden 7 e-mail's moeten zijn");

    }

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
