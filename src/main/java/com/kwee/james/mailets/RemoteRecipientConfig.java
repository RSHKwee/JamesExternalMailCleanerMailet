package com.kwee.james.mailets;

public class RemoteRecipientConfig {
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;
    private String fallbackRecipient;

    // Getters en setters...
    public boolean isReject() { return reject; }
    public void setReject(boolean reject) { this.reject = reject; }
    public boolean isLeaveOnServer() { return leaveOnServer; }
    public void setLeaveOnServer(boolean leaveOnServer) { this.leaveOnServer = leaveOnServer; }
    public boolean isMarkSeen() { return markSeen; }
    public void setMarkSeen(boolean markSeen) { this.markSeen = markSeen; }
    public String getFallbackRecipient() { return fallbackRecipient; }
    public void setFallbackRecipient(String fallbackRecipient) { this.fallbackRecipient = fallbackRecipient; }
}