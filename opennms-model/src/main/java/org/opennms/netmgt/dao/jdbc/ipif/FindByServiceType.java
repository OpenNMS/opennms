package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByServiceType extends IpInterfaceMappingQuery {

    public FindByServiceType(DataSource ds) {
        super(ds, "FROM ipInterface JOIN ifServices ON (ifServices.nodeid = ipInterface.nodeid " +
                  "and ifServices.ipaddr = ipInterface.ipaddr) " +
                  "JOIN service ON (service.serviceid = ifServices.serviceid) " +
                  "where service.servicename = ?");
        declareParameter(new SqlParameter("serviceName", Types.VARCHAR));
        compile();
    }
}