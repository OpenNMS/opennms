/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;


public class FindAllOutages extends OutageMappingQuery {

    public FindAllOutages(DataSource ds) {
        super(ds, "FROM outage as outages");
        compile();
    }
    
}