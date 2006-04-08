/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.ipif;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsIpInterface;

public abstract class FindById extends IpInterfaceMappingQuery {
	
	public static FindById get(DataSource ds, IpInterfaceId id) {
		if (id.getIfIndex() == null)
			return new FindByKeyNullIfIndex(ds);
		else
			return new FindByKeyIfIndex(ds);
	}
    
    public FindById(DataSource ds, String sql) {
        super(ds, sql);
    }
    
    public abstract OnmsIpInterface find(IpInterfaceId id);
    
    
}