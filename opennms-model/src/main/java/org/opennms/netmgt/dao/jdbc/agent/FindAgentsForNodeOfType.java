/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.agent;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindAgentsForNodeOfType extends AgentMappingQuery {

    public FindAgentsForNodeOfType(DataSource ds) {
        super(ds, "FROM " +
        		"node, ipInterface, ifservices " +
        		"WHERE " +
        		"node.nodeid = ipInterface.nodeid " +
        		"and ipInterface.nodeid = ifservices.nodeid " +
        		"and ipInterface.ipAddr = ifservice.ipAddr " +
        		"and ipInterface.ifIndex = ifservices.ifIndex " +
        		"and ipInterface.isSnmpPrimary = 'P' " +
        		"and node.nodeid = ? " +
        		"and ifservices.serviceid = ?"
        		);  
        super.declareParameter(new SqlParameter("nodeid", Types.INTEGER));
        super.declareParameter(new SqlParameter("serviceid", Types.INTEGER));
        compile();
    }
    
}