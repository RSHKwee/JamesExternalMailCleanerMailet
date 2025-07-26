package com.kwee.james.mailets;

public class UndeliverableConfig {
    private boolean leaveOnServer;
    private boolean markSeen;

    public boolean isLeaveOnServer() { return leaveOnServer; }
    public void setLeaveOnServer(boolean leaveOnServer) { this.leaveOnServer = leaveOnServer; }
    public boolean isMarkSeen() { return markSeen; }
    public void setMarkSeen(boolean markSeen) { this.markSeen = markSeen; }
}
