package com.kwee.james.mailets;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
  private String fetchmailFile = "fetchmail.xml";

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
      LOGGER.info("Error cleaning external accounts: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void cleanExternalAccounts() throws Exception {
    // Laad configuratie
    XMLConfiguration config = new Configurations().xml(jamesConfDir + "/" + fetchmailFile);

    // Verwerk alle fetch-blokken
    config.configurationsAt("fetch").forEach(fetchNode -> {
      FetchMailConfig fetchConfig = FetchMailConfig.fromXml(fetchNode);

      // Verwerk accounts
      fetchConfig.getAccounts().forEach(account -> {
        // Maak verbinding en verwijder oude mails
        cleanAccount(fetchConfig.getHost(), fetchConfig.getJavaMailProviderName(), fetchConfig.getJavaMailFolderName(),
            fetchConfig.getJavaMailProperties(), account.getUser(),
//            decryptPassword(account.getPassword(), masterPassword), account.getDaysOld());
            account.getPassword(), account.getDaysOld());
      });
    });
  }

  private void cleanServerAccounts(HierarchicalConfiguration<ImmutableNode> server, String masterPassword) {
    String host = server.getString("host");
    String protocol = server.getString("javaMailProviderName", "imap");
    String folderName = server.getString("javaMailFolderName", "INBOX");

    // Process JavaMail properties
    Properties mailProps = new Properties();
    List<HierarchicalConfiguration<ImmutableNode>> props = server.configurationsAt("javaMailProperties.property");
    for (HierarchicalConfiguration<ImmutableNode> prop : props) {
      mailProps.setProperty(prop.getString("[@name]"), prop.getString("[@value]"));
    }

    List<HierarchicalConfiguration<ImmutableNode>> accounts = server.configurationsAt("accounts.account");

    for (HierarchicalConfiguration<ImmutableNode> account : accounts) {
      String user = account.getString("[@user]");
      String encryptedPass = account.getString("[@password]");
      int daysOld = account.getInt("[@daysOld]", defaultDaysOld);

      try {
        // String password = new OpenSSL(masterPassword).decrypt(encryptedPass);
        String password = "";
        cleanAccount(host, protocol, folderName, mailProps, user, password, daysOld);
      } catch (Exception e) {
        LOGGER.info("Error processing account " + user + ": " + e.getMessage());
      }
    }
  }

  private void cleanAccount(String host, String protocol, String folderName, Properties mailProps, String user,
      String password, int daysOld) {
    try {
      Session session = Session.getInstance(mailProps);
      Store store = session.getStore(protocol);
      store.connect(host, user, password);

      Folder folder = store.getFolder(folderName);
      folder.open(Folder.READ_WRITE);

      Date cutoffDate = Date.from(LocalDate.now().minusDays(daysOld).atStartOfDay(ZoneId.systemDefault()).toInstant());

      Message[] messages = folder.search(new ReceivedDateTerm(ComparisonTerm.LT, cutoffDate));

      if (dryRun) {
        LOGGER.info("DRY RUN: Would delete " + messages.length + " messages for " + user);
      } else {
        folder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
        folder.expunge();
        LOGGER.info("Deleted " + messages.length + " messages for " + user);
      }

      folder.close(false);
      store.close();
    } catch (Exception e) {
      throw new RuntimeException("Error cleaning account " + user, e);
    }
  }

  @Override
  public String getInitParameter(String name, String defaultValue) {
    String value = getInitParameter(name);
    return (value != null) ? value : defaultValue;
  }

  public void setInitParameter(String string, String string2) {
    if (string.equalsIgnoreCase("dryrun")) {
      if (string2.equalsIgnoreCase("true")) {
        dryRun = true;
      } else {
        dryRun = false;
      }
    } else if (string.equalsIgnoreCase("jamesconfdir")) {
      jamesConfDir = string2;
    } else if (string.equalsIgnoreCase("defaultdaysold")) {
      try {
        defaultDaysOld = Integer.parseInt(getInitParameter(string2, "30"));
      } catch (Exception e) {
        defaultDaysOld = 30;
      }
    }
  }

  private static class ReceivedDateTerm extends DateTerm {
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
}