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

package org.opennms.netmgt.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;
import org.opennms.netmgt.graph.dao.api.EntityProperties;

@Entity
@DiscriminatorValue("container")
public class GraphContainerEntity extends AbstractGraphEntity {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_element_relations",
            joinColumns = { @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false, updatable = true) },
            inverseJoinColumns = { @JoinColumn(name="child_id", referencedColumnName = "id", nullable = false, updatable = true) }
    )
    @Where(clause="TYPE='graph'")
    @BatchSize(size=1000)
    private List<GraphEntity> graphs = new ArrayList<>();

    public List<GraphEntity> getGraphs() {
        return graphs;
    }

    public void setGraphs(List<GraphEntity> graphs) {
        this.graphs = graphs;
    }

    public GraphEntity getGraph(String namespace) {
        return graphs.stream()
                .filter(graphEntity -> graphEntity.getNamespace().equals(namespace))
                .findAny().orElseThrow(() -> new NoSuchElementException("No graph with namespace '" + namespace + "' found"));
    }

    public String getLabel() {
        return getPropertyValue(EntityProperties.LABEL);
    }

    public String getDescription() {
        return getPropertyValue(EntityProperties.DESCRIPTION);
    }

    public void removeGraph(String namespace) {
        final GraphEntity graphToRemove = getGraph(namespace);
        getGraphs().remove(graphToRemove);
    }
}
