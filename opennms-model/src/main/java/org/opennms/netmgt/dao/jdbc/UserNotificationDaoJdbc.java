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
package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.UserNotificationDao;
import org.opennms.netmgt.dao.jdbc.usernotification.FindAll;
import org.opennms.netmgt.dao.jdbc.usernotification.FindById;
import org.opennms.netmgt.dao.jdbc.usernotification.LazyUserNotification;
import org.opennms.netmgt.dao.jdbc.usernotification.UserNotificationSaveOrUpdate;
import org.opennms.netmgt.model.OnmsUserNotification;

public class UserNotificationDaoJdbc extends AbstractDaoJdbc implements UserNotificationDao {
    
    public static class Save extends UserNotificationSaveOrUpdate {
        
        // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
        // PARAMETERS IN NotificationSaveOrUpdate
        private static final String insertStmt =
            "insert into usersNotified (" +
            "userID, " +
            "notifyID, " +
            "notifyTime, " +
            "media, " +
            "contactinfo, " +
            "autonotify, " +
            "id" +
            ") values " +
            "(?, ?, ?, ?, ?, ?, ?)";

        
        public Save(DataSource ds) {
            super(ds, insertStmt);
        }
        
        public int doInsert(OnmsUserNotification userNotif) {
            return persist(userNotif);
        }

        
    }
    
    public static class Update extends UserNotificationSaveOrUpdate {

        // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
        // PARAMETERS IN AssetRecordSaveOrUpdate
        private static final String updateStmt =
            "update usersNotified set " +
            "userID = ?, " +
            "notifyID = ?, " +
            "notifyTime = ?, " +
            "media = ?, " +
            "contactinfo = ?, " +
            "autonotify = ? " +
            "where id = ?";

        public Update(DataSource ds) {
            super(ds, updateStmt);
        }
        
        public int doUpdate(OnmsUserNotification userNotif) {
            return persist(userNotif);
        }
        
    }
    

    
    public UserNotificationDaoJdbc() {
        super();
    }
    

    public UserNotificationDaoJdbc(DataSource ds) {
        super(ds);
    }

    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from userNotified");
    }
    
    public Collection findAll() {
        return new FindAll(getDataSource()).execute();
    }
    
    
    public OnmsUserNotification findById(Integer id) {
        return get(id);
    }

    public void flush() {
    }


    public OnmsUserNotification get(int id) {
        return get(new Integer(id));
    }

    public OnmsUserNotification get(Integer id) {
        if (Cache.retrieve(OnmsUserNotification.class, id) == null)
            return new FindById(getDataSource()).findUnique(id);
        else
            return (OnmsUserNotification)Cache.retrieve(OnmsUserNotification.class, id);
    }

    public OnmsUserNotification load(Integer id) {
        OnmsUserNotification userNotif = get(id);
        if (userNotif == null)
            throw new IllegalArgumentException("unable to load user notification with id: "+id);
        return userNotif;
    }

    public void save(OnmsUserNotification userNotif) {
        new Save(getDataSource()).doInsert(userNotif);
    }

    public void update(OnmsUserNotification userNotif) {
    	if (!isDirty(userNotif)) return;
        new Update(getDataSource()).doUpdate(userNotif);
    }


	private boolean isDirty(OnmsUserNotification userNotif) {
		if (userNotif instanceof LazyUserNotification) {
			LazyUserNotification lazyUserNotification = (LazyUserNotification) userNotif;
			return lazyUserNotification.isDirty();
		}
		return true;
	}


    public void saveOrUpdate(OnmsUserNotification userNotif) {
        if (userNotif.getId() == null)
            save(userNotif);
        else
            update(userNotif);
    }

}
