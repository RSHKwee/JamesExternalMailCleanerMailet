package com.kwee.james.mailets;

import java.util.List;

import javax.mail.MessagingException;

import org.apache.james.fetchmail.DynamicAccount;
import org.apache.james.fetchmail.FetchMail;
import org.apache.james.fetchmail.FolderProcessor;
import org.apache.james.fetchmail.MessageProcessor;
import org.apache.james.fetchmail.StoreProcessor;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMailet;

public class ExternalMailCleanupMailet extends GenericMailet {
  private int maxAgeDays;
  private boolean moveToTrashFirst;

  @Override
  public void init() throws MessagingException {
    maxAgeDays = Integer.parseInt(getInitParameter("maxAgeDays"));
    moveToTrashFirst = Boolean.parseBoolean(getInitParameter("moveToTrashFirst"));
  }

  @Override
  public void service(Mail mail) {
    // Haal FetchMail configuratie op (statische accounts)
    List<StaticAccount> accounts = FetchMail.getStaticAccounts();

    for (StaticAccount account : accounts) {
      new Thread(() -> cleanAccount(account)).start();
    }
  }

  private void cleanAccount(StaticAccount account) {
    try {
      StoreProcessor storeProcessor = new StoreProcessor(account);
      storeProcessor.setHost(account.getServer());
      storeProcessor.setPort(account.getPort());
      storeProcessor.setUser(account.getUsername());
      storeProcessor.setPassword(account.getPassword());
      storeProcessor.setProtocol(account.getProtocol());

      // Maak onze aangepaste processor
      MessageProcessor processor = new DeleteOldMessagesProcessor(maxAgeDays, moveToTrashFirst,
          getMailetContext().getLogger());

      // In cleanAccount methode
      if (account.isDynamic()) {
        List<String> users = DynamicAccount.listUsers();
        for (String user : users) {
          DynamicAccount dynAccount = new DynamicAccount(user);
          cleanAccount(dynAccount);
        }
      }
      // Verwerk alle mappen (INBOX, Sent, etc.)
      for (String folderName : account.getFolders()) {
        FolderProcessor folderProcessor = new FolderProcessor();
        folderProcessor.setFolderName(folderName);
        folderProcessor.setMessageProcessor(processor);
        storeProcessor.addProcessor(folderProcessor);
      }

      // Voer de cleanup uit
      storeProcessor.process();

    } catch (Exception e) {
      getMailetContext().getLogger().error("Fout bij opschonen account: " + account.getUsername(), e);
    }
  }
}
