/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.notification;

import javax.sql.DataSource;


public class FindAll extends NotificationMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM notifications as n");
        compile();
    }
    
}