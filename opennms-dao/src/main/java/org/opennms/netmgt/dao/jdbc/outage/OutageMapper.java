package org.opennms.netmgt.dao.jdbc.outage;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.monsvc.MonitoredServiceId;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.jdbc.core.RowMapper;

public class OutageMapper implements RowMapper {


	public OutageMapper() {
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		final Integer id = (Integer) rs.getObject("outages_outageID");
		if (id == null) {
			// this can happen when we are left joining this table with another and there
			// is no corrersponding entry in this table.  If this happens just return null
			return null;
		}
		
		LazyOutage outage = (LazyOutage) Cache.obtain(OnmsOutage.class, id);
		outage.setLoaded(true);
		
		Integer nodeId = new Integer(rs.getInt("outages_nodeID"));
		String ipAddr =  new String (rs.getString("outages_ipAddr"));
		Integer serviceId = new Integer(rs.getInt("outages_serviceID"));
		Integer ifIndex = (Integer) rs.getObject("outages_ifIndex");
		
		MonitoredServiceId monSvcId = new MonitoredServiceId(nodeId, ipAddr, ifIndex, serviceId);
		OnmsMonitoredService monSvc = (OnmsMonitoredService) Cache.obtain(OnmsMonitoredService.class, monSvcId);
		outage.setMonitoredService(monSvc);
		
		Integer lostEventId = new Integer(rs.getInt("outages_svcLostEventID"));
		OnmsEvent lostEvent = (OnmsEvent) Cache.obtain(OnmsEvent.class, lostEventId);
		outage.setServiceLostEvent(lostEvent);
		
		Integer regainedEventId = (Integer) rs.getObject("outages_svcRegainedEventID");
		OnmsEvent regainedEvent = (regainedEventId == null ? null : (OnmsEvent) Cache.obtain(OnmsEvent.class, regainedEventId));
		outage.setServiceRegainedEvent(regainedEvent);
		
		outage.setIfLostService(rs.getTimestamp("outages_ifLostService"));
		outage.setIfRegainedService(rs.getTimestamp("outages_ifRegainedService"));
		
		outage.setSuppressedBy(rs.getString("outages_suppressedBy"));
		
		outage.setSuppressTime(rs.getTimestamp("outages_suppressTime"));
		
		outage.setDirty(false);
		return outage;
	}

}
