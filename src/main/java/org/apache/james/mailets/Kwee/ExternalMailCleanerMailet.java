package org.apache.james.mailets.kwee;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetException;
import org.apache.mailet.base.GenericMailet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mailet for cleaning external mail providers.
 * 
 */
public class ExternalMailCleanerMailet extends GenericMailet {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalMailCleanerMailet.class);

  private String fetchmailFile = "fetchmail.xml";
  private int port = 993;
  private String TLSVersion = "TLSv1.2";

  private String jamesConfDir;
  private int defaultDaysOld;
  private boolean dryRun;
  private String archiveFolder;
  private boolean deleteOriginals;

  @Override
  public void init() throws MailetException {
    jamesConfDir = getInitParameter("jamesConfDir", "/opt/james/conf");
    defaultDaysOld = Integer.parseInt(getInitParameter("defaultDaysOld", "1000"));
    dryRun = Boolean.parseBoolean(getInitParameter("dryRun", "true"));
    archiveFolder = getInitParameter("archiveFolder", "Archived");
    deleteOriginals = Boolean.parseBoolean(getInitParameter("deleteOriginals", "false"));
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

  private void cleanExternalAccounts() throws Exception {
    // Laad configuratie
    XMLConfiguration config = new Configurations().xml(jamesConfDir + "/" + fetchmailFile);

    // Verwerk alle fetch-blokken
    config.configurationsAt("fetch").forEach(fetchNode -> {
      FetchMailConfig fetchConfig = FetchMailConfig.fromXml(fetchNode, defaultDaysOld);

      // Verwerk accounts
      fetchConfig.getAccounts().forEach(account -> {
        Properties properties = new Properties();
        // Default properties for IMAPS
        properties.put("mail.imap.ssl.enable", "false");
        properties.put("mail.imap.host", fetchConfig.getHost());
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.auth", "true");
        properties.put("mail.imap.starttls.enable", "true");
        properties.put("mail.imap.ssl.protocols", TLSVersion);

        // Override with configured properties.
        properties.putAll(fetchConfig.getJavaMailProperties());

        int daysold = fetchConfig.getDaysOld();
        LOGGER.debug("Host: " + fetchConfig.getHost() + "| Days old: " + daysold);

        if (daysold > 0) {
          // Maak verbinding en verwijder oude mails
          cleanAccount(fetchConfig.getHost(), fetchConfig.getJavaMailProviderName(),
              fetchConfig.getJavaMailFolderName(), fetchConfig.getJavaMailProperties(), account.getUser(),
//            decryptPassword(account.getPassword(), masterPassword), account.getDaysOld());
              account.getPassword(), fetchConfig.getDaysOld());
        }
      });
    });
  }

  private void cleanAccount(String host, String protocol, String folderName, Properties mailProps, String user,
      String password, int daysOld) {
    try {
      Session session = Session.getInstance(mailProps);
      Store store = session.getStore(protocol);
      store.connect(host, user, password);

      // Open bronfolder (INBOX)
      Folder sourceFolder = store.getFolder(folderName);
      sourceFolder.open(Folder.READ_WRITE);

      // Bepaal cutoff datum
      Date cutoffDate = Date.from(LocalDate.now().minusDays(daysOld).atStartOfDay(ZoneId.systemDefault()).toInstant());

      // Zoek oude mails
      Message[] oldMessages = sourceFolder.search(new ReceivedDateTerm(ComparisonTerm.LT, cutoffDate));
      if (oldMessages.length > 0) {
        // Maak doelfolder aan indien niet bestaat
        String archiveFolderName = archiveFolder;
        Folder targetFolder = store.getFolder(archiveFolderName);
        if (!targetFolder.exists()) {
          targetFolder.create(Folder.HOLDS_MESSAGES);
          LOGGER.info("Archive folder created: " + archiveFolderName);
        }
        targetFolder.open(Folder.READ_WRITE);

        if (dryRun) {
          LOGGER.info("DRY RUN: Would move " + oldMessages.length + " messages to " + archiveFolderName);
        } else {
          // Verplaats berichten
          sourceFolder.copyMessages(oldMessages, targetFolder);
          LOGGER.info("Moved " + oldMessages.length + " messages to " + archiveFolderName);
          if (deleteOriginals) {
            // Markeer origineel voor verwijdering (optioneel)
            sourceFolder.setFlags(oldMessages, new Flags(Flags.Flag.DELETED), true);
            LOGGER.info("Original messages marked for deletion: " + oldMessages.length);
          }
        }

        // Sluit folders
        targetFolder.close(false);
      }
      sourceFolder.close(false);
      store.close();
    } catch (Exception e) {
      throw new RuntimeException("Error processing account " + user, e);
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