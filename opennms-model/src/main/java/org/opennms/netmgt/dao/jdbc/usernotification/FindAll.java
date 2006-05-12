/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;


public class FindAll extends UserNotificationMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM usersNotified as u");
        compile();
    }
    
}
