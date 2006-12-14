package org.opennms.web.graph;

import java.util.List;

public interface GraphDao {
    public PrefabGraphType findByName(String name);

    public AdhocGraphType findAdhocByName(String name);
    
    public List<PrefabGraph> getAllPrefabGraphs();

    public PrefabGraph getPrefabGraph(String name);
}
