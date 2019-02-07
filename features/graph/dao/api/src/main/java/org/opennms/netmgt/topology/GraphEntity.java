/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.opennms.netmgt.graph.dao.api.EntityProperties;

@Entity
@DiscriminatorValue("graph")
public class GraphEntity extends AbstractGraphEntity {

    // TODO MVR at some point this was vertices and edges. However somehow the @Where(TYPE='edge') and @Where(Type='vertex') did not work when updating data :(
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_element_relations",
            joinColumns = { @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false, updatable = true) },
            inverseJoinColumns = { @JoinColumn(name="child_id", referencedColumnName = "id", nullable = false, updatable = true) }
    )
    @BatchSize(size=1000)
    private List<AbstractGraphEntity> relations = new ArrayList<>();

    // TODO MVR verify that this is properly instantiated when loading data via hibernate
    @Transient
    private Map<String, VertexEntity> vertexIdMap = new HashMap<>();

    public String getDescription() {
        return getPropertyValue(EntityProperties.DESCRIPTION);
    }

    public String getLabel() {
        return getPropertyValue(EntityProperties.LABEL);
    }

    public List<EdgeEntity> getEdges() {
        return getElements(EdgeEntity.class);
    }

    public List<VertexEntity> getVertices() {
        return getElements(VertexEntity.class);
    }

    public VertexEntity getVertexByVertexId(String id) {
        Objects.requireNonNull(id);
        // TODO MVR this is not instantiated properly
//        final VertexEntity vertexEntity = vertexIdMap.get(id);
//        return vertexEntity;
        return getVertexByProperty(EntityProperties.ID, id);
    }

    // TODO MVR this is very slow
    public EdgeEntity getEdgeByProperty(String key, String value) {
        return getEntitiesByProperty(getEdges(), key, value);
    }

    // TODO MVR this is very slow
    public VertexEntity getVertexByProperty(String key, String value) {
        return getEntitiesByProperty(getVertices(), key, value);
    }

    public <T extends AbstractGraphEntity> void addRelations(List<T> someRelations) {
        relations.addAll(someRelations);
    }

    public void addVertex(VertexEntity vertexEntity) {
        relations.add(vertexEntity);
    }

    public void removeVertex(VertexEntity vertexEntity) {
        relations.remove(vertexEntity);
    }

    public void addEdge(EdgeEntity edgeEntity) {
        relations.add(edgeEntity);
    }

    public void removeEdge(EdgeEntity edgeEntity) {
        relations.remove(edgeEntity);
    }

    private <E extends AbstractGraphEntity> E getEntitiesByProperty(List<E> entities, String key, String value) {
        return entities.stream().filter(entity -> entity.getProperties().stream()
                .filter(property -> property.getName().equals(key) && property.getValue().equals(value))
                .findAny()
                .isPresent()
        ).findAny().orElse(null);
    }

    @Transient
    @SuppressWarnings("unchecked")
    private <T extends AbstractGraphEntity> List<T> getElements(Class<T> type) {
        return Collections.unmodifiableList(relations.stream()
                .filter(type::isInstance)
                .map(e -> (T)e)
                .collect(Collectors.toList()));
    }
}
