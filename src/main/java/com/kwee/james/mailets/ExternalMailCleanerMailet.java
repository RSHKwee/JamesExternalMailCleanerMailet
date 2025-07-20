package com.kwee.james.mailets;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.james.util.encrypt.OpenSSL;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetException;
import org.apache.mailet.base.GenericMailet;
import javax.mail.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ExternalMailCleanerMailet extends GenericMailet {
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
            log("Fout bij opschonen externe accounts: " + e.getMessage());
        }
    }

    private void cleanExternalAccounts() throws Exception {
        File fetchmailFile = new File(jamesConfDir, "fetchmail.xml");
        File encryptFile = new File(jamesConfDir, "encrypt.xml");
        
        Configurations configs = new Configurations();
        XMLConfiguration fetchmailConfig = configs.xml(fetchmailFile);
        XMLConfiguration encryptConfig = configs.xml(encryptFile);
        
        String masterPassword = encryptConfig.getString("masterPassword");
        OpenSSL openSSL = new OpenSSL(masterPassword);

        List<HierarchicalConfiguration<ImmutableNode>> accounts = fetchmailConfig.configurationsAt("fetchmail");
        
        for (HierarchicalConfiguration<ImmutableNode> account : accounts) {
            if (account.getBoolean("enabled", true)) {
                cleanAccount(account, openSSL);
            }
        }
    }

    private void cleanAccount(HierarchicalConfiguration<ImmutableNode> account, OpenSSL openSSL) {
        String server = account.getString("server");
        String user = account.getString("userid");
        String encryptedPass = account.getString("password");
        int daysOld = account.getInt("daysOld", defaultDaysOld);
        
        try {
            String password = openSSL.decrypt(encryptedPass);
            cleanImapAccount(server, user, password, daysOld);
        } catch (Exception e) {
            log("Fout bij account " + user + ": " + e.getMessage());
        }
    }

    private void cleanImapAccount(String server, String user, String password, int daysOld) {
        try (Store store = Session.getInstance(System.getProperties()).getStore("imaps")) {
            store.connect(server, user, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Date cutoffDate = Date.from(
                LocalDate.now().minusDays(daysOld)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
            );

            Message[] messages = inbox.search(new ReceivedDateTerm(ComparisonTerm.LT, cutoffDate));
            
            if (dryRun) {
                log("DRY RUN: " + messages.length + " berichten gevonden voor verwijdering in " + user);
            } else {
                inbox.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
                inbox.expunge();
                log("Verwijderd: " + messages.length + " berichten van " + user);
            }
            
            inbox.close(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getInitParameter(String name, String defaultValue) {
        String value = getInitParameter(name);
        return (value != null) ? value : defaultValue;
    }
}