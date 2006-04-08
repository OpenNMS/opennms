/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.snmpif;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNode extends SnmpInterfaceMappingQuery {

    public FindByNode(DataSource ds) {
        super(ds, "FROM snmpinterface where nodeId = ?");
        declareParameter(new SqlParameter("nodeId", Types.INTEGER));
        compile();
    }
}