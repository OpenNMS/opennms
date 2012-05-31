package org.opennms.sandbox;

public class Node {
    
    private String name;
    private String ip;
    private String displayedName;
    private int nodeID;
    
    public Node(int nodeID, String ip, String name) {
        this.nodeID = nodeID;
        this.ip = ip;
        this.name = name;
        displayedName = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIPAddress() {
        return ip;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setIPAddress(String ip) {
        this.ip = ip;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getNodeID() {
        return nodeID;
    }
}
