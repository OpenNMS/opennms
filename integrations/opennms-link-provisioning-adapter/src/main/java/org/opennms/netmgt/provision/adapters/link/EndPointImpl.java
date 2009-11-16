/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import static org.opennms.core.utils.LogUtils.debugf;

import java.net.InetAddress;

import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

public class EndPointImpl implements EndPoint {
    private SnmpAgentConfig m_agentConfig;
    private InetAddress m_address;
    private String m_sysOid;

    public EndPointImpl() {
    }

    public EndPointImpl(InetAddress address, SnmpAgentConfig agentConfig) {
        m_address = address;
        m_agentConfig = agentConfig;
    }

    public SnmpValue get(String oid) {
        SnmpObjId objId = SnmpObjId.get(oid);
        return SnmpUtils.get(m_agentConfig, objId);
    }

    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress address) {
        m_address = address;
    }
    
    public String getSysOid() {
        return m_sysOid;
    }

    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }
    
    public boolean ping() {
        try {
            Long result = Pinger.ping(getAddress());
            if (result != null) {
                return true;
            }
        } catch (Exception e) {
            debugf(this, e, "Ping failed for address %s", getAddress());
        }
        return false;
    }
    
}