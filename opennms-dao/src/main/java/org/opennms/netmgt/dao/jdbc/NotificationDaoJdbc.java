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

import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.dao.jdbc.notification.FindAll;
import org.opennms.netmgt.dao.jdbc.notification.FindByNotifyId;
import org.opennms.netmgt.dao.jdbc.notification.LazyNotification;
import org.opennms.netmgt.dao.jdbc.notification.NotificationSave;
import org.opennms.netmgt.dao.jdbc.notification.NotificationSaveOrUpdate;
import org.opennms.netmgt.model.OnmsNotification;

public class NotificationDaoJdbc extends AbstractDaoJdbc implements NotificationDao {
    
    public static class Save extends NotificationSaveOrUpdate {
        
        // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
        // PARAMETERS IN NotificationSaveOrUpdate
        private static final String insertStmt =
            "insert into notifications (" +
            "textMsg, " +
            "subject, " +
            "numericMsg, " +
            "pageTime, " +
            "respondTime, " +
            "answeredBy, " +
            "nodeID, " +
            "interfaceID, " +
            "serviceID, " +
            "queueID, " +
            "eventID, " +
            "eventUEI, " +
            "notifyID" +
            ") values " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        
        public Save(DataSource ds) {
            super(ds, insertStmt);
        }
        
        public int doInsert(OnmsNotification notification) {
            return persist(notification);
        }

        
    }
    
    public static class Update extends NotificationSaveOrUpdate {

        // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
        // PARAMETERS IN AssetRecordSaveOrUpdate
        private static final String updateStmt =
            "update notifications set " +
            "textMsg = ?, " +
            "subject = ?, " +
            "numericMsg = ?, " +
            "pageTime = ?, " +
            "respondTime = ?, " +
            "answeredBy = ?, " +
            "nodeID = ?, " +
            "interfaceID = ?, " +
            "serviceID = ?, " +
            "queueID = ?, " +
            "eventID = ?, " +
            "eventUEI = ? " +
            "where nodeID = ?";

        public Update(DataSource ds) {
            super(ds, updateStmt);
        }
        
        public int doUpdate(OnmsNotification notification) {
            return persist(notification);
        }
        
    }
    

    
    public NotificationDaoJdbc() {
        super();
    }
    

    public NotificationDaoJdbc(DataSource ds) {
        super(ds);
    }

    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from notifications");
    }
    
    public Collection findAll() {
        return new FindAll(getDataSource()).execute();
    }
    
    
    public OnmsNotification findByNotifyId(Integer id) {
        return get(id);
    }

    public void flush() {
    }

    public OnmsNotification get(Integer id) {
        return new FindByNotifyId(getDataSource()).findUnique(id);
    }

    public OnmsNotification load(Integer id) {
        OnmsNotification notification = get(id);
        if (notification == null)
            throw new IllegalArgumentException("unable to load notification with id: "+id);
        return notification;
    }

//    public void save(OnmsNotification notification) {
//        new Save(getDataSource()).doInsert(notification);
//    }
    public void save(OnmsNotification notif) {
        if (notif.getNotifyId() != null)
            throw new IllegalArgumentException("Cannot save an notification that already has a notifyID");
        
        notif.setNotifyId(allocateNotificationId());
        getNotificationSaver().doInsert(notif);

    }

    private NotificationSave getNotificationSaver() {
        return new NotificationSave(getDataSource());
    }

    private Integer allocateNotificationId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('notifyNxtId')"));
    }

    public void update(OnmsNotification notification) {
    	if (!isDirty(notification)) return;
        new Update(getDataSource()).doUpdate(notification);
    }


	private boolean isDirty(OnmsNotification notification) {
		if (notification instanceof LazyNotification) {
			LazyNotification lazyNotification = (LazyNotification) notification;
			return lazyNotification.isDirty();
		}
		return true;
	}


    public void saveOrUpdate(OnmsNotification notification) {
        if (notification.getNotifyId() == null)
            save(notification);
        else
            update(notification);
    }


	public void delete(OnmsNotification entity) {
		getJdbcTemplate().update("delete from notifications where notifications.notifyId = ?", new Object[] { new Integer(entity.getNotifyId())});
	}

}
