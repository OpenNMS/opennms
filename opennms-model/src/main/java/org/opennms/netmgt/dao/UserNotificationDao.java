package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsUserNotification;

public interface UserNotificationDao extends OnmsDao {

	public abstract OnmsUserNotification load(Integer id);
	
	public abstract void save(OnmsUserNotification notification);
	
	public abstract void update(OnmsUserNotification notification);
    
    public abstract void saveOrUpdate(OnmsUserNotification notification);
    
	public abstract Collection findAll();

}
