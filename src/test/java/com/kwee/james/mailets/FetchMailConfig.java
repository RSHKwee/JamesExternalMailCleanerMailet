package com.kwee.james.mailets;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class FetchMailConfig {
  private String name;
  private String host;
  private long interval;
  private String javaMailProviderName;
  private String javaMailFolderName;
  private Properties javaMailProperties;
  private boolean fetchAll;
  private boolean recurseSubfolders;
  private String defaultDomain;
  private List<Account> accounts = new ArrayList<>();
  private FetchedConfig fetched;
  private RemoteReceivedHeader remoteReceivedHeader;

  private MaxMessageSize maxMessageSize;
  private UndeliverableConfig undeliverable;
  private RecipientNotFoundConfig recipientNotFound;
  private BlacklistConfig blacklist;
  private UserUndefinedConfig userUndefined;
  private RemoteRecipientConfig remoteRecipient;

  // Inner class voor account configuratie
  public static class Account {
    private String user;
    private String password;
    private String recipient;
    private boolean ignoreRcptHeader;
    private int daysOld; // Custom toegevoegd voor mail cleaning

    // Getters en setters
    public String getUser() {
      return user;
    }

    public void setUser(String user) {
      this.user = user;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getRecipient() {
      return recipient;
    }

    public void setRecipient(String recipient) {
      this.recipient = recipient;
    }

    public boolean isIgnoreRcptHeader() {
      return ignoreRcptHeader;
    }

    public void setIgnoreRcptHeader(boolean ignoreRcptHeader) {
      this.ignoreRcptHeader = ignoreRcptHeader;
    }

    public int getDaysOld() {
      return daysOld;
    }

    public void setDaysOld(int daysOld) {
      this.daysOld = daysOld;
    }
  }

  // Overige configuratie classes
  public static class FetchedConfig {
    private boolean leaveOnServer;
    private boolean markSeen;
    // Getters en setters...
  }

  public static class RemoteReceivedHeader {
    private int index;
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;
    // Getters en setters...
  }

  // ... vergelijkbare classes voor andere configuratieblokken

  // Hoofd getters en setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public List<Account> getAccounts() {
    return accounts;
  }

  public void addAccount(Account account) {
    this.accounts.add(account);
  }
  // ... rest van de getters en setters

  // XML Parsing methode
  public static FetchMailConfig fromXml(HierarchicalConfiguration<ImmutableNode> config) {
    FetchMailConfig fetchConfig = new FetchMailConfig();
    fetchConfig.setName(config.getString("[@name]"));
    fetchConfig.setHost(config.getString("host"));
    fetchConfig.setInterval(config.getLong("interval", 600000L));
    fetchConfig.setJavaMailProviderName(config.getString("javaMailProviderName", "imap"));
    fetchConfig.setJavaMailFolderName(config.getString("javaMailFolderName", "INBOX"));

    // Parse JavaMail properties
    Properties mailProps = new Properties();
    config.configurationsAt("javaMailProperties.property")
        .forEach(prop -> mailProps.setProperty(prop.getString("[@name]"), prop.getString("[@value]")));
    fetchConfig.setJavaMailProperties(mailProps);

    // Parse accounts
    config.configurationsAt("accounts.account").forEach(accConfig -> {
      Account account = new Account();
      account.setUser(accConfig.getString("[@user]"));
      account.setPassword(accConfig.getString("[@password]"));
      account.setRecipient(accConfig.getString("[@recipient]"));
      account.setIgnoreRcptHeader(accConfig.getBoolean("[@ignorercpt-header]", false));
      account.setDaysOld(accConfig.getInt("[@daysOld]", 30)); // Default 30 dagen
      fetchConfig.addAccount(account);
    });

    // Parse andere configuratieblokken
    fetchConfig.setFetched(parseFetchedConfig(config.configurationAt("fetched")));
    fetchConfig.setRemoteReceivedHeader(parseRemoteReceivedHeader(config.configurationAt("remotereceivedheader")));
    // ... andere blokken parsen

    return fetchConfig;
  }

  private static FetchedConfig parseFetchedConfig(HierarchicalConfiguration<ImmutableNode> config) {
    FetchedConfig fetched = new FetchedConfig();
    fetched.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    fetched.setMarkSeen(config.getBoolean("[@markseen]", false));
    return fetched;
  }

  private static MaxMessageSize parseMaxMessageSize(HierarchicalConfiguration<ImmutableNode> config) {
    if (config == null) {
      return null;
    }

    MaxMessageSize size = new MaxMessageSize();
    size.setLimit(config.getLong("[@limit]", 0L));
    size.setReject(config.getBoolean("[@reject]", false));
    size.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    size.setMarkSeen(config.getBoolean("[@markseen]", false));
    return size;
  }

  private static BlacklistConfig parseBlacklist(HierarchicalConfiguration<ImmutableNode> config) {
    if (config == null) {
      return null;
    }

    BlacklistConfig blacklist = new BlacklistConfig();
    blacklist.setReject(config.getBoolean("[@reject]", true));
    blacklist.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    blacklist.setMarkSeen(config.getBoolean("[@markseen]", false));

    // Parse blacklist addresses
    String addresses = config.getString(".");
    if (addresses != null) {
      blacklist.setAddresses(List.of(addresses.split("\\s*,\\s*")));
    }

    return blacklist;
  }

}
