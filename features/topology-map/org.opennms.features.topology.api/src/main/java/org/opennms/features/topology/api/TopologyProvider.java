package org.opennms.features.topology.api;

import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;


public interface TopologyProvider {

    void setParent(Object vertexId, Object parentId);

    Object addGroup(String groupIcon);

    boolean containsVertexId(Object vertexId);

    void save(String filename);

    void load(String filename);

    VertexContainer<?, ?> getVertexContainer();

    BeanContainer<?, ?> getEdgeContainer();

    Collection<?> getVertexIds();

    Collection<?> getEdgeIds();

    Item getVertexItem(Object vertexId);

    Item getEdgeItem(Object edgeId);

    Collection<?> getEdgeIdsForVertex(Object vertexId);

    Collection<?> getEndPointIdsForEdge(Object edgeId);

}
