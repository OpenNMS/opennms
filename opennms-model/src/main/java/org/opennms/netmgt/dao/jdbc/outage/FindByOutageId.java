/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByOutageId extends OutageMappingQuery {

    public FindByOutageId(DataSource ds) {
        super(ds, "FROM outages as o WHERE outageid = ?");
        super.declareParameter(new SqlParameter("outageid", Types.INTEGER));
        compile();
    }
    
}