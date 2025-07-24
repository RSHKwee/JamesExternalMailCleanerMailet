package sandbox;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.tree.ImmutableNode;
//import org.apache.james.protocols.lib.PasswordUtil;
//import org.apache.james.util.encrypt.OpenSSL;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetException;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalMailCleanerMailet extends GenericMailet {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalMailCleanerMailet.class);

  private String jamesConfDir;
  private int defaultDaysOld;
  private boolean dryRun;

  @Override
  public void init() throws MailetException {
    jamesConfDir = getInitParameter("jamesConfDir", "/opt/james/conf");
    defaultDaysOld = Integer.parseInt(getInitParameter("defaultDaysOld", "30"));
    dryRun = Boolean.parseBoolean(getInitParameter("dryRun", "false"));
  }

  @Override
  public void service(Mail mail) {
    try {
      cleanExternalAccounts();
    } catch (Exception e) {
      LOGGER.info("Fout bij opschonen externe accounts: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void cleanExternalAccounts() throws Exception {
    File fetchmailFile = new File(jamesConfDir, "fetchmail.xml");
    File encryptFile = new File(jamesConfDir, "encrypt.xml");

    Configurations configs = new Configurations();
    XMLConfiguration fetchmailConfig = configs.xml(fetchmailFile);
    XMLConfiguration encryptConfig = configs.xml(encryptFile);

    String masterPassword = encryptConfig.getString("masterPassword");

    List<HierarchicalConfiguration<ImmutableNode>> accounts = fetchmailConfig.configurationsAt("fetchmail");

    for (HierarchicalConfiguration<ImmutableNode> account : accounts) {
      if (account.getBoolean("enabled", true)) {
        cleanAccount(account, masterPassword);
      }
    }
  }

  private void cleanAccount(HierarchicalConfiguration<ImmutableNode> account, String masterPassword) {
    String server = account.getString("server");
    String user = account.getString("userid");
    String encryptedPass = account.getString("password");
    int daysOld = account.getInt("daysOld", defaultDaysOld);

    try {
      // TODO
      // String password = DecryptWrapper.decrypt(encryptedPass, masterPassword);
      String password = encryptedPass;
      cleanImapAccount(server, user, password, daysOld);
    } catch (Exception e) {
      LOGGER.info("Fout bij account " + user + ": " + e.getMessage());
    }
  }

  private void cleanImapAccount(String server, String user, String password, int daysOld) {
    try {
      Session session = Session.getInstance(System.getProperties());
      Store store = session.getStore("imaps");
      store.connect(server, user, password);

      Folder inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_WRITE);

      Date cutoffDate = Date.from(LocalDate.now().minusDays(daysOld).atStartOfDay(ZoneId.systemDefault()).toInstant());

      Message[] messages = inbox.search(new ReceivedDateTerm(ComparisonTerm.LT, cutoffDate));

      if (dryRun) {
        LOGGER.info("DRY RUN: " + messages.length + " berichten gevonden voor verwijdering in " + user);
      } else {
        inbox.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
        inbox.expunge();
        LOGGER.info("Verwijderd: " + messages.length + " berichten van " + user);
      }

      inbox.close(false);
      store.close();
    } catch (Exception e) {
      throw new RuntimeException("Fout bij IMAP-operatie voor " + user, e);
    }
  }

  @Override
  public String getInitParameter(String name, String defaultValue) {
    String value = getInitParameter(name);
    return (value != null) ? value : defaultValue;
  }

  private static class ReceivedDateTerm extends DateTerm {
    /**
    * 
    */
    private static final long serialVersionUID = -4107976014340562208L;

    public ReceivedDateTerm(int comparison, Date date) {
      super(comparison, date);
    }

    @Override
    public boolean match(Message message) {
      try {
        Date receivedDate = message.getReceivedDate();
        return (receivedDate != null) && super.match(receivedDate);
      } catch (MessagingException e) {
        return false;
      }
    }
  }

  // ============================================================
  public static void main(String[] args) throws IOException {
    ExternalMailCleanerMailet ExtMailet = new ExternalMailCleanerMailet();
    try {
      ExtMailet.cleanExternalAccounts();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}