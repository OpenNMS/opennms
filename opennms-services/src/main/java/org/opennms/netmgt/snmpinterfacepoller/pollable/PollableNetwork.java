package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class PollableNetwork {
    
    private final Map<String, PollableInterface>m_members = new HashMap<String, PollableInterface>();
    private final Map<Integer,String> m_node = new HashMap<Integer, String>();

    private PollContext m_context;
    
    public PollableNetwork(PollContext context) {
        m_context = context;
    }
    
    public PollableInterface create(int nodeid, String ipaddress, String packageName) {
        PollableInterface nodeGroup = new PollableInterface(this);
        nodeGroup.setNodeid(nodeid);
        nodeGroup.setIpaddress(ipaddress);
        nodeGroup.setPackageName(packageName);
        nodeGroup.initialize();
        m_members.put(nodeGroup.getIpaddress(), nodeGroup);
        m_node.put(new Integer(nodeGroup.getNodeid()), nodeGroup.getIpaddress());
        return nodeGroup;
    }
    
    public void deleteAll() {
        Iterator<PollableInterface> ite = m_members.values().iterator();
        while (ite.hasNext()) {
            ite.next().delete();
        }
        m_members.clear();
        m_node.clear();
    }
    
    public void delete(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) {
            m_members.remove(ipaddress);
            m_node.remove(new Integer(pi.getNodeid()));
            pi.delete();
        }
    }
    
    public void refresh(int nodeid) {
        String ipaddress = getIp(nodeid);
        if (ipaddress != null ) getInterface(ipaddress).refresh();
    }
    
    public void suspend(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) pi.suspend();
    }
    
    public void activate(String ipaddress) {
        PollableInterface pi = getInterface(ipaddress);
        if (pi != null) pi.activate();
    }
    
    public String getIp(int nodeid) {
        return m_node.get(new Integer(nodeid));
    }
    
    private PollableInterface getInterface(String ipaddress) {
        if ( m_members.containsKey(ipaddress)) return m_members.get(ipaddress);
        return null;
    }

    public int getNodeid(String ipaddress) {
        Iterator<PollableInterface> ite = m_members.values().iterator();
        while (ite.hasNext()) {
            PollableInterface pi = ite.next();
            if (pi.getIpaddress().equals(ipaddress)) return pi.getNodeid();
        }
        return 0;
    }

    public PollContext getContext() {
        return m_context;
    }

    public void setContext(PollContext context) {
        m_context = context;
    }
}
