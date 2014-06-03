package org.opennms.netmgt.enlinkd;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

public class Dot1dBasePortIfIndexGetter extends TableTracker {

    public final static SnmpObjId DOT1DBASE_PORT_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.17.1.4.1.2");

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	public Dot1dBasePortIfIndexGetter(SnmpAgentConfig peer) {
		m_agentConfig = peer;
	}

	public SnmpValue get(Integer cdpInterfaceIndex) {
		SnmpObjId instance = SnmpObjId.get(new int[] {cdpInterfaceIndex});
		SnmpObjId[] oids = new SnmpObjId[]
				{SnmpObjId.get(DOT1DBASE_PORT_IFINDEX, instance)};
		
		return SnmpUtils.get(m_agentConfig, oids)[0];
	}

}
