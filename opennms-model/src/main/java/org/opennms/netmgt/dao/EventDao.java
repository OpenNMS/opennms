package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsEvent;

public interface EventDao extends OnmsDao {
	
    public abstract void delete(OnmsEvent event);
    
    public abstract Collection findAll();
    
    public abstract OnmsEvent get(int id);
	
	public abstract OnmsEvent get(Integer id);
    
    public abstract OnmsEvent load(int id);

    public abstract OnmsEvent load(Integer id);

    public abstract void save(OnmsEvent event);

    public abstract void saveOrUpdate(OnmsEvent event);

	public abstract void update(OnmsEvent event);
    
}
