package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Set;

import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsNode;

public interface NodeDao extends OnmsDao {
	
    public abstract void delete(OnmsNode node);
    
    public abstract Collection findAll();

    public abstract OnmsNode findByAssetNumber(String string);
    
    public abstract Collection findByLabel(String label);
    
    public abstract Set findNodes(OnmsDistPoller dp);
    
    public abstract OnmsNode get(int id);
	
	public abstract OnmsNode get(Integer id);
    
    public abstract OnmsNode getHierarchy(Integer id);
    
    public abstract OnmsNode load(int id);

    public abstract OnmsNode load(Integer id);

    public abstract void save(OnmsNode node);

    public abstract void saveOrUpdate(OnmsNode node);

	public abstract void update(OnmsNode node);
    
}
