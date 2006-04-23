/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.alarm;

import javax.sql.DataSource;


public class FindAll extends AlarmMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM alarms as a");
        compile();
    }
    
}