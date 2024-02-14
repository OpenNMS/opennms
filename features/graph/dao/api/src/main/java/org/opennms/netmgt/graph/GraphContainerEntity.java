/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
