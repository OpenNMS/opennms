package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByServiceType extends IpInterfaceMappingQuery {

    public FindByServiceType(DataSource ds) {
        super(ds, "FROM ipInterface, ifservices, service where service.servicename = ?");
        declareParameter(new SqlParameter("serviceName", Types.VARCHAR));
        compile();
    }
}