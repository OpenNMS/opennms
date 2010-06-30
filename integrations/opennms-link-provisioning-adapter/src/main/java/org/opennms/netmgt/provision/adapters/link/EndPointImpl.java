
/**
 * <p>EndPointImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
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

    /**
     * <p>Constructor for EndPointImpl.</p>
     */
    public EndPointImpl() {
    }

    /**
     * <p>Constructor for EndPointImpl.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public EndPointImpl(InetAddress address, SnmpAgentConfig agentConfig) {
        m_address = address;
        m_agentConfig = agentConfig;
    }

    /** {@inheritDoc} */
    public SnmpValue get(String oid) {
        SnmpObjId objId = SnmpObjId.get(oid);
        return SnmpUtils.get(m_agentConfig, objId);
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress address) {
        m_address = address;
    }
    
    /**
     * <p>getSysOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysOid() {
        return m_sysOid;
    }

    /**
     * <p>setSysOid</p>
     *
     * @param sysOid a {@link java.lang.String} object.
     */
    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }
    
    /**
     * <p>ping</p>
     *
     * @return a boolean.
     */
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
