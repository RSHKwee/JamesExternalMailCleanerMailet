package sandbox;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apache.james.mailets.Kwee.ExternalMailCleanerMailet;
import org.apache.mailet.MailetConfig;
import org.apache.mailet.MailetContext;
import org.apache.mailet.base.test.FakeMailetConfig;

public class TestMailCleanerMailet {

  // ============================================================
  public static void main(String[] args) throws IOException {
    MailetContext mailetContext;
    ExternalMailCleanerMailet mailet = new ExternalMailCleanerMailet();
    mailetContext = mock(MailetContext.class);
    MailetConfig mailetConfig;

    //@formatter:off
    mailetConfig = FakeMailetConfig.builder()
        .mailetName("ExternMailCleaner")
        .mailetContext(mailetContext)
        .setProperty("jamesConfDir", "F:\\dev\\James Mailets\\ExternalMailCleaner\\src\\test\\resources")
        .setProperty("dryRun", "true")
        .setProperty("defaultDaysOld", "0")
        .setProperty("archiveFolder", "OldMails")
        .setProperty("deleteOriginals", "0")
        .build();
    //@formatter:on

    try {
      mailet.init(mailetConfig);
      mailet.service(null);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}