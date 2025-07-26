package com.kwee.james.mailets;

import java.util.List;

public class BlacklistConfig {
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;
    private List<String> addresses;

    // Getters en setters...
    public boolean isReject() { return reject; }
    public void setReject(boolean reject) { this.reject = reject; }
    public boolean isLeaveOnServer() { return leaveOnServer; }
    public void setLeaveOnServer(boolean leaveOnServer) { this.leaveOnServer = leaveOnServer; }
    public boolean isMarkSeen() { return markSeen; }
    public void setMarkSeen(boolean markSeen) { this.markSeen = markSeen; }
    public List<String> getAddresses() { return addresses; }
    public void setAddresses(List<String> addresses) { this.addresses = addresses; }
}
