/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByInterfaceWithNulIfIndex extends MonitoredServiceMappingQuery {
    public FindByInterfaceWithNulIfIndex(DataSource ds) {
        super(ds, "FROM ifservices where nodeId = ? and ipAddr = ? and ifIndex is null");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        declareParameter(new SqlParameter("ipAddr", Types.VARCHAR));
        compile();
    }
    
    
}