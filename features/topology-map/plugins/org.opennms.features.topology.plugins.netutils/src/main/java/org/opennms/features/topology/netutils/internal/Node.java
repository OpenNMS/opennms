package org.opennms.features.topology.netutils.internal;

/**
 * The Node class constructs an object which contains all necessary information
 * and methods for a server or machine in a network
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class Node {
    
    private String name; //Name of the server or machine
    private String ip; //IP Address of the server or machine
    private int nodeID; //Identification number
    
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
