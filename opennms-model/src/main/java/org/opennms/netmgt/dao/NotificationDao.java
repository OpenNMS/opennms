package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsNotification;

public interface NotificationDao extends OnmsDao {

	public abstract OnmsNotification load(Integer id);
	
	public abstract void save(OnmsNotification notification);
	
	public abstract void update(OnmsNotification notification);
    
    public abstract void saveOrUpdate(OnmsNotification notification);
    
	public abstract Collection findAll();

}
