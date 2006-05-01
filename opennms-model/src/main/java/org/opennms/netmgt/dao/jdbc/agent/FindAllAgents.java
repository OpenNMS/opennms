/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.agent;

import javax.sql.DataSource;


public class FindAllAgents extends AgentMappingQuery {

    public FindAllAgents(DataSource ds) {
        super(ds, "FROM " +
        		"node, ipInterface, ifservices " +
        		"WHERE " +
        		"node.nodeid = ipInterface.nodeid " +
        		"and ipInterface.nodeid = ifservices.nodeid " +
        		"and ipInterface.ipAddr = ifservices.ipAddr " +
        		"and ipInterface.ifIndex = ifservices.ifIndex " +
        		"and ipInterface.isSnmpPrimary = 'P' " +
        	"");  
        compile();
    }
    
}