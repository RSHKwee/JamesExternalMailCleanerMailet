package sandbox;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

public class ConfigTester {
  public static void main(String[] args) throws Exception {
    String fetchdir = "F:\\dev\\James Mailets\\ExternalMailCleaner\\src\\test\\resources";
    XMLConfiguration config = new Configurations().xml(fetchdir + "\\fetchmail.xml");
    System.out.println("Beschikbare keys:");
    config.getKeys().forEachRemaining(System.out::println);

    System.out.println("\nServer configs gevonden:");
    config.configurationsAt("fetch").forEach(s -> {
      System.out.println("Host: " + s.getString("host"));
    });
  }
}
