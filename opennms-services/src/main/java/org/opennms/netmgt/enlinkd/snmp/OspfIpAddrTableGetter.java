package org.opennms.netmgt.enlinkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfLink;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;

public class OspfIpAddrTableGetter extends TableTracker {

    public final static SnmpObjId IPADENT_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.4.20.1.2");
    public final static SnmpObjId IPADENT_NETMASK = SnmpObjId.get(".1.3.6.1.2.1.4.20.1.3");

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	public OspfIpAddrTableGetter(SnmpAgentConfig peer) {
		m_agentConfig = peer;
	}

	public OspfElement get(OspfElement element) {
		SnmpValue[] val = get(element.getOspfRouterId());
		if (val != null && val.length == 2 ) {
			if (!val[0].isNull() && val[0].isNumeric() )
				element.setOspfRouterIdIfindex(val[0].toInt());
			if (!val[1].isNull()) {
				element.setOspfRouterIdNetmask(val[1].toInetAddress());
			}
		}
		return element;
	}
	
	public OspfLink get(OspfLink link) {
		
		SnmpValue[] val = get(link.getOspfIpAddr());
		if (val != null && val.length == 2 ) {
			if (!val[0].isNull() && val[0].isNumeric() )
				link.setOspfIfIndex(val[0].toInt());
			if (!val[1].isNull()) {
				link.setOspfIpMask(val[1].toInetAddress());
			}
		}
		return link;
	}
	private SnmpValue[] get(InetAddress addr) {
		SnmpObjId instance = SnmpObjId.get(addr.getHostAddress());
		SnmpObjId[] oids = new SnmpObjId[]
				{SnmpObjId.get(IPADENT_IFINDEX, instance),
					SnmpObjId.get(IPADENT_NETMASK, instance)};
		
		return SnmpUtils.get(m_agentConfig, oids);
	}

}
