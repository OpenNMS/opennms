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
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId", allocationSize = 1)
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
