package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public interface CollectionAgent extends NetworkInterface {

    public abstract void setMaxVarsPerPdu(int maxVarsPerPdu);

    public abstract int getMaxVarsPerPdu();

    public abstract String getHostAddress();

    public abstract void setSavedIfCount(int ifCount);

    public abstract int getSavedIfCount();

    public abstract int getNodeId();

    public abstract String getSysObjectId();

    public abstract void validateAgent();

    public abstract String toString();

    public abstract SnmpAgentConfig getAgentConfig();

    public abstract Set<IfInfo> getSnmpInterfaceInfo(IfResourceType type);
    
    public abstract InetAddress getInetAddress();

}