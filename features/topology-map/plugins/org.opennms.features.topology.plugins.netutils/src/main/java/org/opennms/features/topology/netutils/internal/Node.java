package org.opennms.features.topology.netutils.internal;

/**
 * The Node class constructs an object which contains all necessary information
 * and methods for a server or machine in a network
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class Node {
    
    private String label; //Name of the server or machine
    private String ipAddr; //IP Address of the server or machine
    private int nodeID; //Identification number
    
    public Node(int nodeID, String ip, String label) {
        this.nodeID = nodeID;
        this.ipAddr = ip;
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getIPAddress() {
        return ipAddr;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setIPAddress(String ip) {
        this.ipAddr = ip;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getNodeID() {
        return nodeID;
    }
}
