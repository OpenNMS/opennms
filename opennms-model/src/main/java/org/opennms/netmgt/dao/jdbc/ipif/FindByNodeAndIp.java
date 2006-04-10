package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNodeAndIp extends IpInterfaceMappingQuery {

    public FindByNodeAndIp(DataSource ds) {
        super(ds, "FROM ipInterface where nodeId = ? and ipaddr = ?");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        declareParameter(new SqlParameter("ipAddr", Types.VARCHAR));
        compile();
    }
}
