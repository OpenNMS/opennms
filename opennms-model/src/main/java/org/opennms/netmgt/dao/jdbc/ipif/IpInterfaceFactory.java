/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.ipif;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class IpInterfaceFactory extends Factory {
	
	public static void register(DataSource dataSource) {
		new IpInterfaceFactory(dataSource);
	}

    public IpInterfaceFactory() {
        super(OnmsIpInterface.class);
    }
    
    public IpInterfaceFactory(DataSource dataSource) {
        this();
		setDataSource(dataSource);
        afterPropertiesSet();
	}
	
	protected void assignId(Object obj, Object id) {
		OnmsIpInterface iface = (OnmsIpInterface) obj;
		IpInterfaceId ifaceId = (IpInterfaceId) id;
		iface.setNode(getNode(ifaceId.getNodeId()));
		iface.setIpAddress(ifaceId.getIpAddr());
		iface.setIfIndex(ifaceId.getIfIndex());
	}

	private OnmsNode getNode(Integer nodeId) {
		return (OnmsNode)Cache.obtain(OnmsNode.class, nodeId);
	}
	
	protected Object create() {
		return new LazyIpInterface(getDataSource());
	}


}