package org.opennms.netmgt.collectd;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

public class SnmpIfData {

    private int m_nodeId;
    private CollectionType m_collectionType;
    private int m_ifIndex;
    private int m_ifType;
    private String m_rrdLabel;
    private String m_ifAlias;

    public SnmpIfData(OnmsSnmpInterface snmpIface) {
        m_nodeId = nullSafeUnbox(snmpIface.getNode().getId(), -1);
        m_collectionType = snmpIface.getCollectionType();
        m_ifIndex = nullSafeUnbox(snmpIface.getIfIndex(), -1);
        m_ifType = nullSafeUnbox(snmpIface.getIfType(), -1);
        m_rrdLabel = snmpIface.computeLabelForRRD();
        m_ifAlias = snmpIface.getIfAlias();
    }
    
    int nullSafeUnbox(Integer num, int dflt) {
        return (num == null ? dflt : num.intValue());
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public CollectionType getCollectionType() {
        return m_collectionType;
    }

    public int getIfIndex() {
        return m_ifIndex;
    }

    public int getIfType() {
        return m_ifType;
    }

    public String getLabelForRRD() {
        return m_rrdLabel;
    }

    public String getIfAlias() {
        return m_ifAlias;
    }

}
