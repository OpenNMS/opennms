package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;

public interface OutageDao extends OnmsDao {

	public abstract OnmsOutage load(Integer id);
	
	public abstract void save(OnmsOutage outage);
	
	public abstract void update(OnmsOutage outage);
    
    public abstract void saveOrUpdate(OnmsOutage outage);
    
	public abstract Collection findAll();

}
