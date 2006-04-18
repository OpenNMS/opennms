package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;

public interface OutageDao extends OnmsDao {

	public abstract OnmsOutage load(Integer id);
	
	public abstract void save(OnmsOutage category);
	
	public abstract void update(OnmsOutage category);
    
    public abstract void saveOrUpdate(OnmsOutage category);
    
	public abstract Collection findAll();

    public abstract OnmsOutage findByName(String name);

}
