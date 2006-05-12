/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.snmpif;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsSnmpInterface;

public abstract class FindById extends SnmpInterfaceMappingQuery {
	
	public static FindById get(DataSource ds, SnmpInterfaceId id) {
		if (id.getIfIndex() == null)
			return new FindByKeyNullIfIndex(ds);
		else
			return new FindByKeyIfIndex(ds);
	}
    
    public FindById(DataSource ds, String clause) {
        super(ds, clause);
    }
    
    public abstract OnmsSnmpInterface find(SnmpInterfaceId id);
    
    
}