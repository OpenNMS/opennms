//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.usernotification;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsUserNotification;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class UserNotificationSaveOrUpdate extends SqlUpdate {

    public UserNotificationSaveOrUpdate(DataSource ds, String updateStmt) {
        setDataSource(ds);
        setSql(updateStmt);
        
        // assumes that the update and insert statements have the same parms in the same order
        declareParameter(new SqlParameter(Types.TIMESTAMP));  //notifyTime
        declareParameter(new SqlParameter(Types.VARCHAR));  //media
        declareParameter(new SqlParameter(Types.VARCHAR));  //contactInfo
        declareParameter(new SqlParameter(Types.VARCHAR));  //autoNotify
        declareParameter(new SqlParameter(Types.VARCHAR));  //userID
        declareParameter(new SqlParameter(Types.INTEGER));  //notifyID
        compile();
    }
    
    public int persist(OnmsUserNotification userNotification) {
        Object[] parms = new Object[] {
        		userNotification.getNotifyTime(), //notifyTime
             userNotification.getMedia(), //media
             userNotification.getContactInfo(), //contactInfo
             userNotification.getAutoNotify(), //autoNotify
             userNotification.getUserId(), //userID
             userNotification.getNotification().getNotifyId() //notifyID
        };
        return update(parms);
    }    

}
