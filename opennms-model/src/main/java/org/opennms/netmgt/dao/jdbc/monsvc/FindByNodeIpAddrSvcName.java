package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNodeIpAddrSvcName extends MonitoredServiceMappingQuery {

	public FindByNodeIpAddrSvcName(DataSource ds) {
		super(ds, "FROM ifservices, service where ifservices.serviceid = service.serviceid and ifservices.nodeId = ? and ifservices.ipAddr = ? and service.servicename = ?");
		declareParameter(new SqlParameter("nodeId", Types.INTEGER));
		declareParameter(new SqlParameter("ipAddr", Types.VARCHAR));
		declareParameter(new SqlParameter("servicename", Types.VARCHAR));
		compile();
	}
}

