package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;

public interface GraphDao {
    public PrefabGraphType findByName(String name);

    public AdhocGraphType findAdhocByName(String name);
    
    public List<PrefabGraph> getAllPrefabGraphs();

    public PrefabGraph getPrefabGraph(String name);
    
    public PrefabGraph[] getPrefabGraphsForResource(OnmsResource resource);
}
