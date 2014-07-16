package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

public class CdpInterfacePortNameGetter extends TableTracker {

    public final static SnmpObjId CDP_INTERFACE_NAME = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.1.1.1.6");

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	public CdpInterfacePortNameGetter(SnmpAgentConfig peer) {
		m_agentConfig = peer;
	}

	public CdpLink get(CdpLink link) {
		SnmpObjId instance = SnmpObjId.get(new int[] {link.getCdpCacheIfIndex()});
		SnmpObjId[] oids = new SnmpObjId[]
				{SnmpObjId.get(CDP_INTERFACE_NAME, instance)};
		
		SnmpValue[] val = SnmpUtils.get(m_agentConfig, oids);
		if (val == null || val.length != 1 || val[0] == null)
			return link;
		link.setCdpInterfaceName(val[0].toDisplayString());
		return link;
	}

}
