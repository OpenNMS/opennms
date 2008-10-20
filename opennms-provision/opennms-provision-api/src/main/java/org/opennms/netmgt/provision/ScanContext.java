package org.opennms.netmgt.provision;

import java.net.InetAddress;

public interface ScanContext {

    /**
     * Return the preferred address used to talk to the agent of type type provided
     * 
     * e.g.  use getAgentAddress("SNMP") to find the InetAddress for the SNMP Agent for the node being scanned.
     * 
     * @param agentType the type of agent to search for
     * @return the InetAddress for the agent or null if no such agent exists
     */
    public InetAddress getAgentAddress(String agentType);
    
    public void updateSysObjectId(String sysObjectId);
}
