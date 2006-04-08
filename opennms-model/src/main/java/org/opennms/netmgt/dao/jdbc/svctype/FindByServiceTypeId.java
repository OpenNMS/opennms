/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.svctype;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByServiceTypeId extends ServiceTypeMappingQuery {
    
    public FindByServiceTypeId(DataSource ds) {
        super(ds, "FROM service WHERE serviceid = ?");
        super.declareParameter(new SqlParameter("serviceid", Types.INTEGER));
        compile();
    }
}