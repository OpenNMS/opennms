/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByIpInterface extends IpInterfaceMappingQuery {

    public FindByIpInterface(DataSource ds) {
        super(ds, "FROM ipInterface where ipAddr = ?");
        declareParameter(new SqlParameter("ipAddr", Types.VARCHAR));
        compile();
    }
}