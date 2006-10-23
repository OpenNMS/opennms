package org.opennms.web.graph;

public interface GraphDao {
    public PrefabGraphType findByName(String name);

    public AdhocGraphType findAdhocByName(String name);
}
