package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.springframework.jdbc.core.RowMapper;

public class IpInterfaceMapper implements RowMapper {
	
	public IpInterfaceMapper() {
	}
	
	public OnmsIpInterface mapInterface(ResultSet rs, int rowNum) throws SQLException {
		return (OnmsIpInterface)mapRow(rs, rowNum);
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Integer nodeId = (Integer)rs.getObject("ipInterface_nodeID");  //nodeID                  integer,
		if (nodeId == null) {
			// this can happen when we are left joining this table with another and there
			// is no corrersponding entry in this table.  If this happens just return null
			return null;
		}
		String ipAddr = rs.getString("ipInterface_ipAddr");            //ipAddr                  varchar(16) not null,
		Integer ifIndex = (Integer)rs.getObject("ipInterface_ifIndex");//ifIndex                 integer,
		
		final IpInterfaceId key = new IpInterfaceId(nodeId, ipAddr, ifIndex);
		
		LazyIpInterface iface = (LazyIpInterface)Cache.obtain(OnmsIpInterface.class, key);
		iface.setLoaded(true);
		iface.setIpHostName(rs.getString("ipInterface_ipHostname"));              //ipHostname              varchar(256),
		iface.setIsManaged(rs.getString("ipInterface_isManaged"));                //isManaged               char(1),
		iface.setIpStatus(((Integer)rs.getObject("ipInterface_ipStatus")));       //ipStatus                integer,
		iface.setIpLastCapsdPoll(rs.getTimestamp("ipInterface_ipLastCapsdPoll")); //ipLastCapsdPoll         timestamp without time zone,
		iface.setIsSnmpPrimary(CollectionType.get(rs.getString("ipInterface_isSnmpPrimary")));        //isSnmpPrimary           char(1),
		
		setMonitoredServices(iface);
		
		iface.setDirty(false);
		return iface;
	}

	protected void setMonitoredServices(OnmsIpInterface iface) {
		// we don't do anything by default!
	}

}
