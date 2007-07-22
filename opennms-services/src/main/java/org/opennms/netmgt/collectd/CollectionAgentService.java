package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public interface CollectionAgentService {

    public abstract String getHostAddress();

    public abstract int getNodeId();
    
    public abstract int getIfIndex();

    public abstract String getSysObjectId();

    public abstract CollectionType getCollectionType();
    
    public abstract SnmpAgentConfig getAgentConfig();

    public abstract Set<SnmpIfData> getSnmpInterfaceData();
    
    public abstract InetAddress getInetAddress();

}