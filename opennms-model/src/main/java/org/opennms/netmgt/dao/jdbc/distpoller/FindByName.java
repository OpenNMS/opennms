/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.distpoller;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByName extends DistPollerMappingQuery {

    public FindByName(DataSource ds) {
        super(ds, "FROM distPoller WHERE distPoller.dpName = ?");
        super.declareParameter(new SqlParameter("dpName", Types.VARCHAR));
        compile();
    }
    
}