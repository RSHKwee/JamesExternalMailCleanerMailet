package com.kwee.james.mailets;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message; // JavaMail-API voor berichtmanipulatie
import javax.mail.MessagingException;

import org.apache.james.fetchmail.MessageProcessor; // Kernklasse voor berichtverwerking
import org.apache.james.mailbox.MailboxSession;
import org.slf4j.Logger;

public class DeleteOldMessagesProcessor extends MessageProcessor {
  private final int maxAgeDays;
  private final boolean moveToTrashFirst;
  private final Logger logger;

  public DeleteOldMessagesProcessor(int maxAgeDays, boolean moveToTrashFirst, Logger logger) {

    this.maxAgeDays = maxAgeDays;
    this.moveToTrashFirst = moveToTrashFirst;
    this.logger = logger;
  }

  @Override
  public boolean process(Message message, MailboxSession session) {
    try {
      // Bepaal de maximale leeftijd
      Instant threshold = Instant.now().minus(maxAgeDays, ChronoUnit.DAYS);

      // Controleer berichtdatum (gebruik sentDate als receivedDate niet beschikbaar
      // is)
      Date msgDate = message.getReceivedDate() != null ? message.getReceivedDate() : message.getSentDate();

      if (msgDate == null || msgDate.toInstant().isAfter(threshold)) {
        return false; // Bericht is niet oud genoeg
      }

      // Verplaats eerst naar prullenbak indien geconfigureerd
      if (moveToTrashFirst && !isInTrashFolder(message)) {
        moveToTrashFolder(message);
        logger.info("Bericht verplaatst naar prullenbak: " + message.getSubject());
        return true;
      }

      // Permanent verwijderen
      message.setFlag(Flags.Flag.DELETED, true);
      logger.info("Bericht gemarkeerd voor verwijdering: " + message.getSubject());
      return true;

    } catch (MessagingException e) {
      logger.error("Fout bij verwerken bericht", e);
      return false;
    }
  }

  private boolean isInTrashFolder(Message message) throws MessagingException {
    Folder folder = message.getFolder();
    return folder.getName().equalsIgnoreCase("Trash") || folder.getName().equalsIgnoreCase("Bin");
  }

  private void moveToTrashFolder(Message message) throws MessagingException {
    Folder currentFolder = message.getFolder();
    Folder trashFolder = currentFolder.getStore().getFolder("Trash");

    if (!trashFolder.exists()) {
      trashFolder = currentFolder.getStore().getFolder("Bin");
    }

    if (trashFolder.exists()) {
      currentFolder.copyMessages(new Message[] { message }, trashFolder);
    }

    // Markeer origineel bericht voor verwijdering
    message.setFlag(Flags.Flag.DELETED, true);
  }
}