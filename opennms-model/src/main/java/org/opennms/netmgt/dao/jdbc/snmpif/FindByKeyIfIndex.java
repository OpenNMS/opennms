/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.snmpif;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.jdbc.core.SqlParameter;

public class FindByKeyIfIndex extends FindById {
    
    public FindByKeyIfIndex(DataSource ds) {
        super(ds, "FROM ipInterface where nodeId = ? and ipAddr = ? and ifIndex = ?");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        declareParameter(new SqlParameter("ipAdddr", Types.VARCHAR));
        declareParameter(new SqlParameter("ifIndex", Types.INTEGER));
        compile();
    }
    
    public OnmsSnmpInterface find(SnmpInterfaceId id) {
    	return findUnique(id.getNodeId(), id.getIpAddr(), id.getIfIndex());
    }
}