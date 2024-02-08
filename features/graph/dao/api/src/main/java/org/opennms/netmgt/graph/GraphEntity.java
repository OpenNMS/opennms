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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.opennms.netmgt.graph.dao.api.EntityProperties;

@Entity
@DiscriminatorValue("graph")
public class GraphEntity extends AbstractGraphEntity {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "graph_element_relations",
            joinColumns = { @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false, updatable = true) },
            inverseJoinColumns = { @JoinColumn(name="child_id", referencedColumnName = "id", nullable = false, updatable = true) }
    )
    @BatchSize(size=1000)
    private List<AbstractGraphEntity> relations = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="focus_id")
    private FocusEntity defaultFocus;

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

    public FocusEntity getDefaultFocus() {
        return defaultFocus;
    }

    public void setDefaultFocus(FocusEntity defaultFocus) {
        this.defaultFocus = defaultFocus;
    }

    public VertexEntity getVertexByVertexId(String id) {
        Objects.requireNonNull(id);
        return getVertexByProperty(EntityProperties.ID, id);
    }

    public EdgeEntity getEdgeByProperty(String key, String value) {
        return getEntitiesByProperty(getEdges(), key, value);
    }

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

    @Transient
    @SuppressWarnings("unchecked")
    private <T extends AbstractGraphEntity> List<T> getElements(Class<T> type) {
        return Collections.unmodifiableList(relations.stream()
                .filter(type::isInstance)
                .map(e -> (T)e)
                .collect(Collectors.toList()));
    }

    private static <E extends AbstractGraphEntity> E getEntitiesByProperty(List<E> entities, String key, String value) {
        return entities.stream().filter(entity -> entity.getProperties().stream()
                .filter(property -> property.getName().equals(key) && property.getValue().equals(value))
                .findAny()
                .isPresent()
        ).findAny().orElse(null);
    }
}
