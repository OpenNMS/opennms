/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.agent;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindAgentsForNode extends AgentMappingQuery {

    public FindAgentsForNode(DataSource ds) {
        super(ds, "FROM " +
        		"node, ipInterface, ifservices " +
        		"WHERE " +
        		"node.nodeid = ipInterface.nodeid " +
        		"and ipInterface.nodeid = ifservices.nodeid " +
        		"and ipInterface.ipAddr = ifservice.ipAddr " +
        		"and ipInterface.ifIndex = ifservices.ifIndex " +
        		"and ipInterface.isSnmpPrimary = 'P' " +
        		"and node.nodeid = ? " +
        	"");  
        super.declareParameter(new SqlParameter("nodeid", Types.INTEGER));
        compile();
    }
    
}