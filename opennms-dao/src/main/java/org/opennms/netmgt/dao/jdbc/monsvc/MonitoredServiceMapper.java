package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.jdbc.core.RowMapper;

public class MonitoredServiceMapper implements RowMapper {

	public MonitoredServiceMapper() {
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Integer nodeId = (Integer)rs.getObject("ifservices_nodeid");       //nodeID                  integer,        
		if (nodeId == null) {
			// this can happen when we are left joining this table with another and there
			// is no corrersponding entry in this table.  If this happens just return null
			return null;
		}
		String ipAddr  = rs.getString("ifservices_ipAddr");                //ipAddr                  varchar(16) not null,
		Integer ifIndex = (Integer)rs.getObject("ifservices_ifIndex");     //ifIndex                 integer,
		Integer serviceId = (Integer)rs.getObject("ifservices_serviceId"); //serviceID               integer,
		
		final MonitoredServiceId id = new MonitoredServiceId(nodeId, ipAddr, ifIndex, serviceId);
		
		LazyMonitoredService svc = (LazyMonitoredService)Cache.obtain(OnmsMonitoredService.class, id);
		svc.setLoaded(true);
		svc.setLastGood(rs.getTimestamp("ifservices_lastGood"));           //lastGood                timestamp without time zone,
		svc.setLastFail(rs.getTimestamp("ifservices_lastFail"));           //lastFail                timestamp without time zone,
		svc.setQualifier(rs.getString("ifservices_qualifier"));            //qualifier               char(16),
		svc.setStatus(rs.getString("ifservices_status"));                  //status                  char(1),
		svc.setSource(rs.getString("ifservices_source"));                  //source                  char(1),
		svc.setNotify(rs.getString("ifservices_notify"));                  //notify                  char(1),
		
		setCurrentOutages(svc);
		
		svc.setDirty(false);
		return svc;
	}

	protected void setCurrentOutages(OnmsMonitoredService svc) {
        svc.setCurrentOutages(new LinkedHashSet<OnmsOutage>());
	}

}
