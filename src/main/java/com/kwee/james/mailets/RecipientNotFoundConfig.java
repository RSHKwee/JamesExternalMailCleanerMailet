package com.kwee.james.mailets;

public class RecipientNotFoundConfig {
    private boolean defer;
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;

    // Getters en setters...
    public boolean isDefer() { return defer; }
    public void setDefer(boolean defer) { this.defer = defer; }
    public boolean isReject() { return reject; }
    public void setReject(boolean reject) { this.reject = reject; }
    public boolean isLeaveOnServer() { return leaveOnServer; }
    public void setLeaveOnServer(boolean leaveOnServer) { this.leaveOnServer = leaveOnServer; }
    public boolean isMarkSeen() { return markSeen; }
    public void setMarkSeen(boolean markSeen) { this.markSeen = markSeen; }
}
