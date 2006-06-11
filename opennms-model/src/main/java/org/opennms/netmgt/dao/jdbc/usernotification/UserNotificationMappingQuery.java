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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;
import org.springframework.jdbc.object.MappingSqlQuery;

public class UserNotificationMappingQuery extends MappingSqlQuery {

    public UserNotificationMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT " + 
             "u.userID as userID, " +
             "u.notifyID as notifyID, " +
             "u.notifyTime as notifyTime, " +
             "u.media as media, " +
             "u.contactinfo as contactinfo, " +
             "u.autonotify as autonotify, " +
             "u.id as id" + clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        
        final Integer id = (Integer) rs.getObject("id");
        
        LazyUserNotification userNotification = (LazyUserNotification)Cache.obtain(OnmsUserNotification.class, id);
        userNotification.setLoaded(true);
        
        final String userID = rs.getString("userID");
        userNotification.setUserId(userID);
        final Integer notifyID = (Integer) rs.getObject("notifyID");
        OnmsNotification notif = (OnmsNotification)Cache.obtain(OnmsNotification.class, notifyID);
        userNotification.setNotification(notif);
        userNotification.setNotifyTime(rs.getTimestamp("notifyTime"));
        userNotification.setMedia(rs.getString("media"));
        userNotification.setContactInfo(rs.getString("contactinfo"));
        userNotification.setAutoNotify(rs.getString("autonotify"));
       
        userNotification.setDirty(false);
        return userNotification;
    }
    
    public OnmsUserNotification findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsUserNotification findUnique(Object o1) {
        return findUnique(new Object[] { o1 });
    }

    public OnmsUserNotification findUnique(Object o1, Object o2) {
        return findUnique(new Object[] { o1, o2 });
    }

    public OnmsUserNotification findUnique(Object[] objs) {
        List userNotifications = execute(objs);
        if (userNotifications.size() > 0)
            return (OnmsUserNotification)userNotifications.get(0);
        else
            return null;
    }
    
    public Set findSet() {
        return findSet((Object[])null);
    }
    
    public Set findSet(Object obj) {
        return findSet(new Object[] { obj });
    }
    
    public Set findSet(Object[] objs) {
        List events = execute(objs);
        Set results = new JdbcSet(events);
        return results;
    }
    
}
