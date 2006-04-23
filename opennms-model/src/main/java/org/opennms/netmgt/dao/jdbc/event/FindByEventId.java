/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.event;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByEventId extends EventMappingQuery {

    public FindByEventId(DataSource ds) {
        super(ds, "FROM events as e WHERE eventid = ?");
        super.declareParameter(new SqlParameter("eventid", Types.INTEGER));
        compile();
    }
    
}