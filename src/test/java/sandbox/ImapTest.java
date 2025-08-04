package sandbox;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class ImapTest {
  public static void main(String[] args) {
    // Start zowel IMAP (poort 3143) als SMTP (poort 3025)
    GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP_IMAP); // Combinatie van SMTP + IMAP
    greenMail.start();

    // Stel een testgebruiker in
    greenMail.setUser("test@localhost", "gebruikersnaam", "wachtwoord");

    // Verstuur een testmail (gebruik nu de juiste poort)
    GreenMailUtil.sendTextEmail("test@localhost", // Naar
        "sender@localhost", // Van
        "Testonderwerp", "Testinhoud", greenMail.getSmtp().getServerSetup() // Gebruik SMTP-configuratie
    );

    greenMail.stop();
  }
}