/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.agent;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindAgentsForType extends AgentMappingQuery {

    public FindAgentsForType(DataSource ds) {
        super(ds, "FROM " +
        		"node, ipInterface, ifservices " +
        		"WHERE " +
        		"node.nodeid = ipInterface.nodeid " +
        		"and ipInterface.nodeid = ifservices.nodeid " +
        		"and ipInterface.ipAddr = ifservice.ipAddr " +
        		"and ipInterface.ifIndex = ifservices.ifIndex " +
        		"and ipInterface.isSnmpPrimary = 'P' " +
        		"and ifservices.serviceid = ? " +
        	"");  
        declareParameter(new SqlParameter("serviceid", Types.INTEGER));
        compile();
    }
    
}