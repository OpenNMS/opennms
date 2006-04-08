/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.ipif;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNode extends IpInterfaceMappingQuery {

    public FindByNode(DataSource ds) {
        super(ds, "FROM ipInterface where nodeId = ?");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        compile();
    }
}