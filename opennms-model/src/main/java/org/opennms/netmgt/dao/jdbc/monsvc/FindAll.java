/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.monsvc;

import javax.sql.DataSource;


public class FindAll extends MonitoredServiceMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM ifservices");
        compile();
    }
}