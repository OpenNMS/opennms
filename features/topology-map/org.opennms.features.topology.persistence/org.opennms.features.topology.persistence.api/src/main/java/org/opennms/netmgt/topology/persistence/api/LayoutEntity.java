/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
