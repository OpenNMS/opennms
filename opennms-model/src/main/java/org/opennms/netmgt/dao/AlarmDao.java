package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsAlarm;

public interface AlarmDao extends OnmsDao {
	
    public abstract void delete(OnmsAlarm alarm);
    
    public abstract Collection findAll();
    
    public abstract OnmsAlarm get(int id);
	
	public abstract OnmsAlarm get(Integer id);
    
    public abstract OnmsAlarm load(int id);

    public abstract OnmsAlarm load(Integer id);

    public abstract void save(OnmsAlarm alarm);

    public abstract void saveOrUpdate(OnmsAlarm alarm);

	public abstract void update(OnmsAlarm alarm);
    
}
