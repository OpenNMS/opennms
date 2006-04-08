/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.distpoller;

import javax.sql.DataSource;


public class FindAll extends DistPollerMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM distPoller");
        compile();
    }
    
}