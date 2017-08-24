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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="topo_vertex_position")
public class VertexPositionEntity {

    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    @Column(name = "id", nullable = false)
    private int id;

    @Embedded
    private VertexRefEntity vertexRef;

    @Embedded
    private PointEntity position;

    public VertexPositionEntity() {

    }

    public VertexPositionEntity(VertexRefEntity vertexRefEntity, PointEntity pointEntity) {
        setPosition(pointEntity);
        setVertexRef(vertexRefEntity);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PointEntity getPosition() {
        return position;
    }

    public void setPosition(PointEntity position) {
        this.position = position;
    }

    public VertexRefEntity getVertexRef() {
        return vertexRef;
    }

    public void setVertexRef(VertexRefEntity vertexRef) {
        this.vertexRef = vertexRef;
    }
}
