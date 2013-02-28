package org.opennms.netmgt.linkd.snmp;

import java.util.List;

import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.model.OnmsVlan.VlanStatus;

public interface VlanTable {
    /**
     * The VLAN string to define default VLAN name
     */
    public final static String DEFAULT_VLAN_NAME = "default";

    /**
     * The VLAN int to define default VLAN index
     */
    public final static int DEFAULT_VLAN_INDEX = 1;

    /**
     * The VLAN int to define default VLAN status, 1 means operational/active
     */
	public static final VlanStatus DEFAULT_VLAN_STATUS = VlanStatus.CISCOVTP_OPERATIONAL;

	public List<OnmsVlan> getVlansForSnmpCollection();
	
}
