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
package org.opennms.netmgt.topology.persistence.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="topo_layout")
public class LayoutEntity {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(name="created", nullable = false)
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="creator", nullable=false)
    private String creator;

    @Column(name="updated", nullable=false)
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date updated;

    @Column(name="updator", nullable=false)
    private String updator;

    /**
     * The last time, when this layout was "read"
     */
    @Column(name="last_used")
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date lastUsed;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch= FetchType.EAGER)
    @JoinTable(name="topo_layout_vertex_positions",
            joinColumns = @JoinColumn(name="layout_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="vertex_position_id", referencedColumnName = "id"))
    private List<VertexPositionEntity> vertexPositions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUpdator() {
        return updator;
    }

    public void setUpdator(String updator) {
        this.updator = updator;
    }

    public List<VertexPositionEntity> getVertexPositions() {
        return vertexPositions;
    }

    public void setVertexPositions(List<VertexPositionEntity> vertexPositions) {
        this.vertexPositions = vertexPositions;
    }

    public PointEntity getPosition(String namespace, String id) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);
        Optional<VertexPositionEntity> first = vertexPositions.stream()
                .filter(e -> e.getVertexRef().getNamespace().equals(namespace) && e.getVertexRef().getId().equals(id))
                .findFirst();
        if (first.isPresent()) {
            VertexPositionEntity vertexPositionEntity = first.get();
            return vertexPositionEntity.getPosition();
        }
        return null;
    }

    public void addVertexPosition(VertexPositionEntity position) {
        Objects.requireNonNull(position);
        getVertexPositions().add(position);
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Date getLastUsed() {
        return lastUsed;
    }
}
