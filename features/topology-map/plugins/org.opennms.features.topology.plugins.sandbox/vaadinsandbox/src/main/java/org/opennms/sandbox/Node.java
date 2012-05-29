package org.opennms.sandbox;

public class Node {
    
    String name;
    String ip;
    private String displayedName;
    
    public Node(String ip, String name) {
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
}
