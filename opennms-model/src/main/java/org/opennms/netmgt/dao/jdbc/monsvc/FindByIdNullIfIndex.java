package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.springframework.jdbc.core.SqlParameter;

public class FindByIdNullIfIndex extends FindById {

	public FindByIdNullIfIndex(DataSource ds) {
		super(ds, "FROM ifservices where nodeId = ? and ipAddr = ? and ifIndex is null and serviceId = ?");
		declareParameter(new SqlParameter("nodeId", Types.INTEGER));
		declareParameter(new SqlParameter("ipAddr", Types.VARCHAR));
		declareParameter(new SqlParameter("serviceId", Types.INTEGER));
		compile();
	}

	public OnmsMonitoredService find(MonitoredServiceId id) {
		return findUnique(new Object[] { id.getNodeId(), id.getIpAddr(), id.getServiceId() });
	}

}
