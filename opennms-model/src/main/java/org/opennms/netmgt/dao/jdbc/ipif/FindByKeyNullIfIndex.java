/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.jdbc.core.SqlParameter;

public class FindByKeyNullIfIndex extends FindById {

    public FindByKeyNullIfIndex(DataSource ds) {
        super(ds, "FROM ipInterface where nodeId = ? and ipAddr = ? and ifIndex is null");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        declareParameter(new SqlParameter("ipAdddr", Types.VARCHAR));
        compile();
    }

	public OnmsIpInterface find(IpInterfaceId id) {
		return findUnique(id.getNodeId(), id.getIpAddr());
	}
}