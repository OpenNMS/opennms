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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;
import org.opennms.features.graph.api.generic.GenericProperties;

@Entity
@DiscriminatorValue("graph")
public class GraphEntity extends AbstractGraphEntity {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_element_relations",
            joinColumns = { @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name="child_id", referencedColumnName = "id", nullable = false, updatable = false) }
    )
    @Where(clause="TYPE='vertex'")
    @BatchSize(size=1000)
    private List<VertexEntity> vertices = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_element_relations",
            joinColumns = { @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name="child_id", referencedColumnName = "id", nullable = false, updatable = false) }
    )
    @Where(clause="TYPE='edge'")
    @BatchSize(size=1000)
    private List<EdgeEntity> edges = new ArrayList<>();

    // TODO MVR verify that this is properly instantiated when loading data via hibernate
    @Transient
    private Map<String, VertexEntity> vertexIdMap = new HashMap<>();

    public String getDescription() {
        final PropertyEntity property = getProperty(GenericProperties.DESCRIPTION);
        if (property != null) {
            return property.getValue();
        }
        return null;
    }

    public String getLabel() {
        final PropertyEntity property = getProperty(GenericProperties.LABEL);
        if (property != null) {
            return property.getValue();
        }
        return null;
    }

    public List<EdgeEntity> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeEntity> edges) {
        this.edges = edges;
    }

    public List<VertexEntity> getVertices() {
        return vertices;
    }

    public void setVertices(List<VertexEntity> vertices) {
        this.vertices = vertices;
        vertices.forEach(v -> {
            final String vertexId = v.getProperty(GenericProperties.ID).getValue();
            vertexIdMap.put(vertexId, v);
        });
    }

    public VertexEntity getVertexByVertexId(String id) {
        Objects.requireNonNull(id);
        final VertexEntity vertexEntity = vertexIdMap.get(id);
        return vertexEntity;
    }
}
