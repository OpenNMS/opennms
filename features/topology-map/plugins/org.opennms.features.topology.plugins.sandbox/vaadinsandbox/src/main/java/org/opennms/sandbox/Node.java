package org.opennms.sandbox;

public class Node {
    
    private String name;
    private String ip;
    private int nodeID;
    
    public Node(int nodeID, String ip, String name) {
        this.nodeID = nodeID;
        this.ip = ip;
        this.name = name;
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

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getNodeID() {
        return nodeID;
    }
}
