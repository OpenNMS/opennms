/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.snmpif;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new SnmpInterfaceFactory(dataSource);
	}

    public SnmpInterfaceFactory() {
        super(OnmsSnmpInterface.class);
    }
    
    public SnmpInterfaceFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}
	
	protected void assignId(Object obj, Object id) {
		OnmsSnmpInterface iface = (OnmsSnmpInterface) obj;
		SnmpInterfaceId ifaceId = (SnmpInterfaceId) id;
		iface.setNode(getNode(ifaceId.getNodeId()));
		iface.setIpAddress(ifaceId.getIpAddr());
		iface.setIfIndex(ifaceId.getIfIndex());
	}

	private OnmsNode getNode(Integer nodeId) {
		return (OnmsNode)Cache.obtain(OnmsNode.class, nodeId);
	}

	protected Object create() {
		return new LazySnmpInterface(getDataSource());
	}


}