package org.apache.james.mailets.kwee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Load FetchMail.xml into a Java class.
 */
public class FetchMailConfig {
  /**
   * Fetch mail configuration XML to objects
   */
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
  private int daysOld;

  /**
   * Account configuration
   */
  public static class Account {
    private String user;
    private String password;
    private String recipient;
    private boolean ignoreRcptHeader;

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
  }

  /**
   * Configuration parts
   */
  public static class FetchedConfig {
    private boolean leaveOnServer;
    private boolean markSeen;

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }
  }

  public static class RemoteReceivedHeader {
    private int index;
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;

    public int getIndex() {
      return index;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public boolean isReject() {
      return reject;
    }

    public void setReject(boolean reject) {
      this.reject = reject;
    }

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }
  }

  public static class MaxMessageSize {
    private long limit;
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;

    public long getLimit() {
      return limit;
    }

    public void setLimit(long limit) {
      this.limit = limit;
    }

    public boolean isReject() {
      return reject;
    }

    public void setReject(boolean reject) {
      this.reject = reject;
    }

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }
  }

  public static class UndeliverableConfig {
    private boolean leaveOnServer;
    private boolean markSeen;

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }
  }

  public static class RecipientNotFoundConfig {
    private boolean defer;
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;

    public boolean isDefer() {
      return defer;
    }

    public void setDefer(boolean defer) {
      this.defer = defer;
    }

    public boolean isReject() {
      return reject;
    }

    public void setReject(boolean reject) {
      this.reject = reject;
    }

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }
  }

  public static class BlacklistConfig {
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;
    private List<String> addresses;

    public boolean isReject() {
      return reject;
    }

    public void setReject(boolean reject) {
      this.reject = reject;
    }

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }

    public List<String> getAddresses() {
      return addresses;
    }

    public void setAddresses(List<String> addresses) {
      this.addresses = addresses;
    }
  }

  public static class UserUndefinedConfig {
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;

    public boolean isReject() {
      return reject;
    }

    public void setReject(boolean reject) {
      this.reject = reject;
    }

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }
  }

  public static class RemoteRecipientConfig {
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;
    private String fallbackRecipient;

    public boolean isReject() {
      return reject;
    }

    public void setReject(boolean reject) {
      this.reject = reject;
    }

    public boolean isLeaveOnServer() {
      return leaveOnServer;
    }

    public void setLeaveOnServer(boolean leaveOnServer) {
      this.leaveOnServer = leaveOnServer;
    }

    public boolean isMarkSeen() {
      return markSeen;
    }

    public void setMarkSeen(boolean markSeen) {
      this.markSeen = markSeen;
    }

    public String getFallbackRecipient() {
      return fallbackRecipient;
    }

    public void setFallbackRecipient(String fallbackRecipient) {
      this.fallbackRecipient = fallbackRecipient;
    }
  }

  /**
   * Getters and setters for Main part
   */
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

  public long getInterval() {
    return interval;
  }

  public void setInterval(long interval) {
    this.interval = interval;
  }

  public String getJavaMailProviderName() {
    return javaMailProviderName;
  }

  public void setJavaMailProviderName(String javaMailProviderName) {
    this.javaMailProviderName = javaMailProviderName;
  }

  public String getJavaMailFolderName() {
    return javaMailFolderName;
  }

  public void setJavaMailFolderName(String javaMailFolderName) {
    this.javaMailFolderName = javaMailFolderName;
  }

  public Properties getJavaMailProperties() {
    return javaMailProperties;
  }

  public void setJavaMailProperties(Properties javaMailProperties) {
    this.javaMailProperties = javaMailProperties;
  }

  public boolean isFetchAll() {
    return fetchAll;
  }

  public void setFetchAll(boolean fetchAll) {
    this.fetchAll = fetchAll;
  }

  public boolean isRecurseSubfolders() {
    return recurseSubfolders;
  }

  public void setRecurseSubfolders(boolean recurseSubfolders) {
    this.recurseSubfolders = recurseSubfolders;
  }

  public String getDefaultDomain() {
    return defaultDomain;
  }

  public void setDefaultDomain(String defaultDomain) {
    this.defaultDomain = defaultDomain;
  }

  public int getDaysOld() {
    return daysOld;
  }

  public void setDaysOld(int daysOld) {
    this.daysOld = daysOld;
  }

  public List<Account> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<Account> accounts) {
    this.accounts = accounts;
  }

  public void addAccount(Account account) {
    this.accounts.add(account);
  }

  public FetchedConfig getFetched() {
    return fetched;
  }

  public void setFetched(FetchedConfig fetched) {
    this.fetched = fetched;
  }

  public RemoteReceivedHeader getRemoteReceivedHeader() {
    return remoteReceivedHeader;
  }

  public void setRemoteReceivedHeader(RemoteReceivedHeader remoteReceivedHeader) {
    this.remoteReceivedHeader = remoteReceivedHeader;
  }

  public MaxMessageSize getMaxMessageSize() {
    return maxMessageSize;
  }

  public void setMaxMessageSize(MaxMessageSize maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
  }

  public UndeliverableConfig getUndeliverable() {
    return undeliverable;
  }

  public void setUndeliverable(UndeliverableConfig undeliverable) {
    this.undeliverable = undeliverable;
  }

  public RecipientNotFoundConfig getRecipientNotFound() {
    return recipientNotFound;
  }

  public void setRecipientNotFound(RecipientNotFoundConfig recipientNotFound) {
    this.recipientNotFound = recipientNotFound;
  }

  public BlacklistConfig getBlacklist() {
    return blacklist;
  }

  public void setBlacklist(BlacklistConfig blacklist) {
    this.blacklist = blacklist;
  }

  public UserUndefinedConfig getUserUndefined() {
    return userUndefined;
  }

  public void setUserUndefined(UserUndefinedConfig userUndefined) {
    this.userUndefined = userUndefined;
  }

  public RemoteRecipientConfig getRemoteRecipient() {
    return remoteRecipient;
  }

  public void setRemoteRecipient(RemoteRecipientConfig remoteRecipient) {
    this.remoteRecipient = remoteRecipient;
  }

  /**
   * XML Parsing methods
   * 
   * @param config
   * @param a_defaultDays
   * @return
   */
  public static FetchMailConfig fromXml(HierarchicalConfiguration<ImmutableNode> config, int a_defaultDays) {
    return fromXmlLocal(config, a_defaultDays);
  }

  public static FetchMailConfig fromXml(HierarchicalConfiguration<ImmutableNode> config) {
    return fromXmlLocal(config, -1); // Default Days old -1, keep messages.
  }

  private static FetchMailConfig fromXmlLocal(HierarchicalConfiguration<ImmutableNode> config, int defaultDaysOld) {
    FetchMailConfig fetchConfig = new FetchMailConfig();
    fetchConfig.setName(config.getString("[@name]", ""));
    fetchConfig.setHost(config.getString("host", ""));
    fetchConfig.setInterval(config.getLong("interval", 600000L));
    fetchConfig.setJavaMailProviderName(config.getString("javaMailProviderName", "imap"));
    fetchConfig.setJavaMailFolderName(config.getString("javaMailFolderName", "INBOX"));
    fetchConfig.setFetchAll(config.getBoolean("fetchall", true));
    fetchConfig.setRecurseSubfolders(config.getBoolean("recursesubfolders", false));
    fetchConfig.setDefaultDomain(config.getString("defaultdomain", ""));
    fetchConfig.setDaysOld(config.getInt("daysOld", defaultDaysOld)); // Default -1 dagen, oftewel altijd bewaren...

    // Parse JavaMail properties
    Properties mailProps = new Properties();
    List<HierarchicalConfiguration<ImmutableNode>> props = config.configurationsAt("javaMailProperties.property");
    for (HierarchicalConfiguration<ImmutableNode> prop : props) {
      mailProps.setProperty(prop.getString("[@name]", ""), prop.getString("[@value]", ""));
    }
    fetchConfig.setJavaMailProperties(mailProps);

    // Parse accounts
    List<HierarchicalConfiguration<ImmutableNode>> accounts = config.configurationsAt("accounts.account");
    for (HierarchicalConfiguration<ImmutableNode> accConfig : accounts) {
      Account account = new Account();
      account.setUser(accConfig.getString("[@user]", ""));
      account.setPassword(accConfig.getString("[@password]", ""));
      account.setRecipient(accConfig.getString("[@recipient]", ""));
      account.setIgnoreRcptHeader(accConfig.getBoolean("[@ignorercpt-header]", false));
      fetchConfig.addAccount(account);
    }

    // Parse andere configuratieblokken
    if (config.configurationsAt("fetched").size() > 0) {
      fetchConfig.setFetched(parseFetchedConfig(config.configurationAt("fetched")));
    }

    if (config.configurationsAt("remotereceivedheader").size() > 0) {
      fetchConfig.setRemoteReceivedHeader(parseRemoteReceivedHeader(config.configurationAt("remotereceivedheader")));
    }

    if (config.configurationsAt("maxmessagesize").size() > 0) {
      fetchConfig.setMaxMessageSize(parseMaxMessageSize(config.configurationAt("maxmessagesize")));
    }

    if (config.configurationsAt("undeliverable").size() > 0) {
      fetchConfig.setUndeliverable(parseUndeliverableConfig(config.configurationAt("undeliverable")));
    }

    if (config.configurationsAt("recipientnotfound").size() > 0) {
      fetchConfig.setRecipientNotFound(parseRecipientNotFoundConfig(config.configurationAt("recipientnotfound")));
    }

    if (config.configurationsAt("blacklist").size() > 0) {
      fetchConfig.setBlacklist(parseBlacklistConfig(config.configurationAt("blacklist")));
    }

    if (config.configurationsAt("userundefined").size() > 0) {
      fetchConfig.setUserUndefined(parseUserUndefinedConfig(config.configurationAt("userundefined")));
    }

    if (config.configurationsAt("remoterecipient").size() > 0) {
      fetchConfig.setRemoteRecipient(parseRemoteRecipientConfig(config.configurationAt("remoterecipient")));
    }

    return fetchConfig;
  }

  /**
   * Parsers for configuration parts
   */
  private static FetchedConfig parseFetchedConfig(HierarchicalConfiguration<ImmutableNode> config) {
    FetchedConfig fetched = new FetchedConfig();
    fetched.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    fetched.setMarkSeen(config.getBoolean("[@markseen]", false));
    return fetched;
  }

  private static RemoteReceivedHeader parseRemoteReceivedHeader(HierarchicalConfiguration<ImmutableNode> config) {
    RemoteReceivedHeader header = new RemoteReceivedHeader();
    header.setIndex(config.getInt("[@index]", 1));
    header.setReject(config.getBoolean("[@reject]", true));
    header.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    header.setMarkSeen(config.getBoolean("[@markseen]", false));
    return header;
  }

  private static MaxMessageSize parseMaxMessageSize(HierarchicalConfiguration<ImmutableNode> config) {
    MaxMessageSize size = new MaxMessageSize();
    size.setLimit(config.getLong("[@limit]", 0L));
    size.setReject(config.getBoolean("[@reject]", false));
    size.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    size.setMarkSeen(config.getBoolean("[@markseen]", false));
    return size;
  }

  private static UndeliverableConfig parseUndeliverableConfig(HierarchicalConfiguration<ImmutableNode> config) {
    UndeliverableConfig undeliverable = new UndeliverableConfig();
    undeliverable.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    undeliverable.setMarkSeen(config.getBoolean("[@markseen]", false));
    return undeliverable;
  }

  private static RecipientNotFoundConfig parseRecipientNotFoundConfig(HierarchicalConfiguration<ImmutableNode> config) {
    RecipientNotFoundConfig rnf = new RecipientNotFoundConfig();
    rnf.setDefer(config.getBoolean("[@defer]", true));
    rnf.setReject(config.getBoolean("[@reject]", true));
    rnf.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    rnf.setMarkSeen(config.getBoolean("[@markseen]", false));
    return rnf;
  }

  private static BlacklistConfig parseBlacklistConfig(HierarchicalConfiguration<ImmutableNode> config) {
    BlacklistConfig blacklist = new BlacklistConfig();
    blacklist.setReject(config.getBoolean("[@reject]", true));
    blacklist.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    blacklist.setMarkSeen(config.getBoolean("[@markseen]", false));

    // Parse addresses
    String addressStr = config.getString(".");
    if (addressStr != null && !addressStr.isEmpty()) {
      blacklist.setAddresses(Arrays.asList(addressStr.trim().split("\\s*,\\s*")));
    }
    return blacklist;
  }

  private static UserUndefinedConfig parseUserUndefinedConfig(HierarchicalConfiguration<ImmutableNode> config) {
    UserUndefinedConfig userUndef = new UserUndefinedConfig();
    userUndef.setReject(config.getBoolean("[@reject]", true));
    userUndef.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    userUndef.setMarkSeen(config.getBoolean("[@markseen]", false));
    return userUndef;
  }

  private static RemoteRecipientConfig parseRemoteRecipientConfig(HierarchicalConfiguration<ImmutableNode> config) {
    RemoteRecipientConfig remoteRecipient = new RemoteRecipientConfig();
    remoteRecipient.setReject(config.getBoolean("[@reject]", false));
    remoteRecipient.setLeaveOnServer(config.getBoolean("[@leaveonserver]", true));
    remoteRecipient.setMarkSeen(config.getBoolean("[@markseen]", false));
    remoteRecipient.setFallbackRecipient(config.getString(".", ""));
    return remoteRecipient;
  }
}