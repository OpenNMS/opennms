/*
 * Created on Nov 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.poller;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class IfKey {
    private int m_nodeId;

    private String m_ipAddr;

    public IfKey(int nodeId, String ipAddr) {
        m_nodeId = nodeId;
        m_ipAddr = ipAddr;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

}
