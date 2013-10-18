package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.OnmsVlan;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpStore;

public abstract class VlanTableBasic extends SnmpTable<SnmpStore> implements VlanTable {

	protected VlanTableBasic(InetAddress address, String tableName,
			NamedSnmpVar[] columns) {
		super(address, tableName, columns);
	}

        @Override
	public List<OnmsVlan> getVlansForSnmpCollection() {
		List<OnmsVlan> vlans = new ArrayList<OnmsVlan>();
		vlans.add(new OnmsVlan(
                VlanTable.DEFAULT_VLAN_INDEX,
                VlanTable.DEFAULT_VLAN_NAME,
                VlanTable.DEFAULT_VLAN_STATUS));
		return vlans;
	}

}
