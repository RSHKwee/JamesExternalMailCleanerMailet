package com.kwee.james.mailets;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.james.core.builder.MimeMessageBuilder;
import org.apache.james.server.core.MailImpl;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetContext;
//import org.apache.mailet.base.test.FakeMail;
// import org.apache.mailet.base.test.FakeMail;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.mail.MessagingException;

public class KPNVeiligVirusScanTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(KPNVeiligVirusScanTest.class);

  private KPNVeiligVirusScan mailet;
  private MailetContext mailetContext;
  private FakeMailetConfig mailetConfig;

  @BeforeMethod
  public void setUp() throws Exception {
    mailet = mock(KPNVeiligVirusScan.class);
    mailetContext = mock(MailetContext.class);

    //@formatter:off
    mailetConfig = FakeMailetConfig.builder()
        .mailetName("KPNVeiligScan")
        .mailetContext(mailetContext)
        .setProperty("kpnVeiligPath", "C:\\Program Files (x86)\\KPN Veilig\\fsscan.exe")
        .setProperty("tmpDir", "target\\tmp")
        .setProperty("quarantineDir", "target/quarantine")
        .build();
    //@formatter:on
  }

  @Test
  public void testCleanMailShouldContinueProcessing() throws Exception {
    LOGGER.info("testCleanMailShouldContinueProcessing");
    // Arrange
    mailet.init(mailetConfig);
    Mail mail = mock(Mail.class);
    mail = Mockito.spy(createTestMail());

    mailet.service(mail);
    // Mock de scanner om false (schoon) terug te geven
//    KPNVeiligVirusScan spyMailet = spy(mailet);
//    spyMailet.service(mail);

    // Assert
    assertNull(mail.getState(), "Mail state should not be changed");
  }

  @Test
  public void testInfectedMailShouldBeQuarantined() throws Exception {
    LOGGER.info("testInfectedMailShouldBeQuarantined");
    // Arrange
    mailet.init(mailetConfig);
    Mail mail = Mockito.spy(createInfectedMail());

    // Mock de scanner om true (geïnfecteerd) terug te geven
//    KPNVeiligVirusScan spyMailet = spy(mailet);
//  doReturn(true).when(spyMailet).scanFileWithKPNVScan(any());

    // Act
    mailet.service(mail);
    // spyMailet.service(mail);

    // Assert
    Mockito.verify(mail).setState(Mail.GHOST);
    assertEquals(mail.getState(), Mail.GHOST, "Mail should be ghosted");

  }

  @Test(expectedExceptions = MessagingException.class)
  public void testScanFailureShouldThrowException() throws Exception {
    LOGGER.info("testScanFailureShouldThrowException");
    // Arrange
    mailet.init(mailetConfig);
    Mail mail = Mockito.spy(createTestMail());

    // Mock de scanner om een exception te gooien
    // KPNVeiligVirusScan spyMailet = spy(mailet);
    // doThrow(new MessagingException("Scan
    // error")).when(spyMailet).scanFileWithKPNVScan(any());

    // Act
    mailet.service(mail);
    Mockito.verify(mail).setState(Mail.GHOST);
    // spyMailet.service(mail);
  }

  @Test
  public void testQuarantineDisabled() throws Exception {
    LOGGER.info("testQuarantineDisabled");
    // Arrange
    //@formatter:off
    mailetConfig = FakeMailetConfig.builder()
        .mailetName("KPNVeiligVirusScan")
        .mailetContext(mailetContext)
        .setProperty("quarantine", "false")
        .setProperty("tmpDir", "target\\tmp")
        .build();
    mailet.init(mailetConfig);
    // formatter:on
    
    Mail mail = Mockito.spy(createInfectedMail());
    // KPNVeiligVirusScan spyMailet = spy(mailet);

    // Act
    mailet.service(mail);
//    spyMailet.service(mail);

    // Assert
    Mockito.verify(mail).setState(Mail.GHOST);
    assertEquals(mail.getState(), Mail.GHOST, "Mail should be ghosted");
    // Verify no move to quarantine happened
    // (In echte implementatie zou je filesystem checks doen)
  }

// ==================================================================
// Local routines
//
  private Mail createTestMail() {
    javax.mail.internet.MimeMessage message;
    try {
      message = MimeMessageBuilder.mimeMessageBuilder().setSubject("Test mail").setText("Hello world!").build();
      //@formatter:off
      return (Mail) MailImpl.builder()
          .name("mail1")
          .mimeMessage(message)
          .sender("sender@domain.com")
          .build();
      //@formatter:on
    } catch (Exception e) {
      // TODO Auto-generated catch block
      LOGGER.info(e.getMessage().toString());
      // e.printStackTrace();
    }
    return null;
  }

  private Mail createInfectedMail() throws MessagingException, IOException {
    Properties props = new Properties();
    Session session = Session.getInstance(props);

    // 1. Maak een MimeMessage
    MimeMessage mimeMessage = new MimeMessage(session);
    try {
      // 2. Stel headers in
      mimeMessage.setHeader("Date", "Fri, 18 Jul 2025 14:14:03 +0200 (CEST)");
      mimeMessage.setHeader("Message-ID", "<1601756706.0.1752840843239>");
      mimeMessage.setSubject("Test: EICAR Virus Test File");
      mimeMessage.setFrom(new InternetAddress("sender@example.com"));
      mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("recipient@example.com"));
      mimeMessage.setHeader("MIME-Version", "1.0");

      // 3. Maak een multipart-bericht met boundary
      MimeMultipart multipart = new MimeMultipart("mixed");
      multipart.setSubType("mixed");

      // 4. Voeg de EICAR-bijlage toe
      MimeBodyPart attachmentPart = new MimeBodyPart();
      String eicarContent = reverse("*H+H$!ELIF-TSET-SURIVITNA-DRADNATS-RACIE$}7)CC7)^P(45XZP\\4[PA@%P!O5X");

      attachmentPart.setContent(eicarContent, "application/octet-stream");
      attachmentPart.setFileName("eicar.com");
      attachmentPart.setHeader("Content-ID", "<c6d1e995-d4ac-4ab9-8b50-7f4568b9c499>");
      multipart.addBodyPart(attachmentPart);

      mimeMessage.setContent(multipart);

      // 5. Schrijf het bericht naar een bestand (of verstuur het)
      // mimeMessage.writeTo(System.out); // Print naar console (of gebruik
      // FileOutputStream)

      // 6. Maak het Mail object
      //@formatter:off
      Mail mail = (Mail) MailImpl.builder()
          .name("virus-test-mail")
          .mimeMessage(mimeMessage)
          .sender("sender@domain.com")
          .build();
      //@formatter:on

      Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"));
      Path tempFile = Files.createTempFile(tempPath, "scan-", ".eml");
      try (OutputStream out = Files.newOutputStream(tempFile)) {
        mail.getMessage().writeTo(out);
      }
      LOGGER.debug("Virusmail File: " + tempFile.toAbsolutePath().toString());

      return mail;
    } catch (Exception e) {
      LOGGER.info(e.getMessage().toString());
    }
    return null;
  }

  private String reverse(String str) {
    if (str.isEmpty()) {
      return str;
    }
    return reverse(str.substring(1)) + str.charAt(0);
  }

}