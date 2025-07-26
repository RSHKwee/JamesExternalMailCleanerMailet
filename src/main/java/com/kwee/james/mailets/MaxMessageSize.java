package com.kwee.james.mailets;

public class MaxMessageSize {
    private long limit;
    private boolean reject;
    private boolean leaveOnServer;
    private boolean markSeen;

    // Getters en setters
    public long getLimit() { return limit; }
    public void setLimit(long limit) { this.limit = limit; }
    public boolean isReject() { return reject; }
    public void setReject(boolean reject) { this.reject = reject; }
    public boolean isLeaveOnServer() { return leaveOnServer; }
    public void setLeaveOnServer(boolean leaveOnServer) { this.leaveOnServer = leaveOnServer; }
    public boolean isMarkSeen() { return markSeen; }
    public void setMarkSeen(boolean markSeen) { this.markSeen = markSeen; }
}
