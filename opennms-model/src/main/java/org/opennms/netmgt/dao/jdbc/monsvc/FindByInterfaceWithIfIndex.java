/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByInterfaceWithIfIndex extends MonitoredServiceMappingQuery {
    public FindByInterfaceWithIfIndex(DataSource ds) {
        super(ds, "FROM ifservices where nodeId = ? and ipAddr = ? and ifIndex = ?");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        declareParameter(new SqlParameter("ipAddr", Types.VARCHAR));
        declareParameter(new SqlParameter("ifIndex", Types.INTEGER));
        compile();
    }
    
    
}