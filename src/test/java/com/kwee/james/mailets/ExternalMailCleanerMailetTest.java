package com.kwee.james.mailets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
//import org.apache.james.util.encrypt.OpenSSL;
import org.apache.mailet.Mail;
import org.apache.mailet.MailetContext;
import org.apache.mailet.base.test.FakeMailetConfig;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExternalMailCleanerMailetTest {
  private ExternalMailCleanerMailet mailet;
  private MailetContext mailetContext;
  private HierarchicalConfiguration<ImmutableNode> config;
  private FakeMailetConfig mailetConfig;

  @BeforeMethod
  public void setUp() throws Exception {
    mailet = new ExternalMailCleanerMailet();
    mailetContext = mock(MailetContext.class);
    //@formatter:off
    mailetConfig = FakeMailetConfig.builder()
        .mailetName("ExternMailCleaner")
        .mailetContext(mailetContext)
        .setProperty("jamesConfDir", "/opt/james/conf")
        .setProperty("dryRun", "false")
        .setProperty("defaultDaysOld", "30")
        .build();
    //@formatter:on

    mailet = new ExternalMailCleanerMailet();
    mailetContext = mock(MailetContext.class);
    config = mock(HierarchicalConfiguration.class);
  }

  @Test
  public void testInitShouldSetDefaultValues() throws MessagingException {
    mailet.init(mailetConfig);
    assertEquals(mailet.getInitParameter("jamesConfDir"), "/opt/james/conf");
    assertEquals(mailet.getInitParameter("defaultDaysOld"), "30");
    assertEquals(mailet.getInitParameter("dryRun"), "false");
  }

  @Test
  public void testServiceShouldNotThrowWithValidInput() throws MessagingException {
    mailet.init(mailetConfig);
    Mail mail = mock(Mail.class);
    mailet.service(mail); // Should not throw
  }

  @Test
  public void testCleanImapAccountDryRun() throws Exception {
    mailet.init(mailetConfig);
    // Mock IMAP server
    Store store = mock(Store.class);
    Folder folder = mock(Folder.class);
    when(store.getFolder("INBOX")).thenReturn(folder);
    when(folder.search(any())).thenReturn(new Message[] { new MimeMessage((Session) null) });

    // Test dry run mode
    mailet.setInitParameter("dryRun", "true");
    // mailet.cleanImapAccount("imap.test.com", "user@test.com", "password", 30);

    // verify(folder, never()).setFlags(any(), any(), anyBoolean());
  }

  @Test
  public void testDecryptPassword() throws Exception {
    String encrypted = "U2FsdGVkX1+..."; // Versleutelde testwaarde
    String master = "masterpassword";

    // OpenSSL openSSL = mock(OpenSSL.class);
    // when(openSSL.decrypt(encrypted)).thenReturn("decryptedPassword");

    // Gebruik reflectie om de decryptie te testen
    String result = Whitebox.invokeMethod(mailet, "decryptPassword", encrypted, master);
    assertEquals(result, "decryptedPassword");
  }

  @Test(expectedExceptions = MessagingException.class)
  public void testCleanImapAccountShouldThrowOnConnectionFailure() throws Exception {
    mailet.init(mailetConfig);
    // mailet.cleanImapAccount("invalid.server", "user", "pass", 30);
  }
}
