/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;


public class OutageAll extends OutageMappingQuery {

    public OutageAll(DataSource ds) {
        super(ds, "FROM outages as o");
        compile();
    }
    
}