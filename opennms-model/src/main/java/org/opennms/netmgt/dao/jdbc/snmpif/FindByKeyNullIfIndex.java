/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.snmpif;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.jdbc.core.SqlParameter;

public class FindByKeyNullIfIndex extends FindById {

    public FindByKeyNullIfIndex(DataSource ds) {
        super(ds, "FROM snmpinterface where nodeid = ? and ipaddr = ? and snmpifindex is null");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        declareParameter(new SqlParameter("ipAdddr", Types.VARCHAR));
        compile();
    }

	public OnmsSnmpInterface find(SnmpInterfaceId id) {
		return findUnique(id.getNodeId(), id.getIpAddr());
	}
}