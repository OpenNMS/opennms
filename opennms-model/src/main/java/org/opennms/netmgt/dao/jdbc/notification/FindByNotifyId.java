/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.notification;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNotifyId extends NotificationMappingQuery {

    public FindByNotifyId(DataSource ds) {
        super(ds, "FROM notifications as n WHERE notifyID = ?");
        super.declareParameter(new SqlParameter("notifyID", Types.INTEGER));
        compile();
    }
    
}