/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindById extends UserNotificationMappingQuery {

    public FindById(DataSource ds) {
        super(ds, "FROM usersNotified as u WHERE u.id = ?");
        super.declareParameter(new SqlParameter("id", Types.INTEGER));
        compile();
    }
    
}