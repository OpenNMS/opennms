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

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("edge")
public class EdgeEntity extends AbstractGraphEntity {

    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "namespace", column = @Column(name = "source_vertex_namespace")),
            @AttributeOverride(name = "id", column = @Column(name = "source_vertex_id"))
    })
    private VertexRefEntity source;

    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "namespace", column = @Column(name = "target_vertex_namespace")),
            @AttributeOverride(name = "id", column = @Column(name = "target_vertex_id"))
    })
    private VertexRefEntity target;

    public VertexRefEntity getSource() {
        return source;
    }

    public void setSource(String namespace, String id) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);
        setSource(new VertexRefEntity(namespace, id));
    }

    public void setSource(VertexRefEntity source) {
        Objects.requireNonNull(source, "source can not be null");
        this.source = source;
    }

    public VertexRefEntity getTarget() {
        return target;
    }

    public void setTarget(String namespace, String id) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);
        setTarget(new VertexRefEntity(namespace, id));
    }

    public void setTarget(VertexRefEntity target) {
        Objects.requireNonNull(target, "target can not be null");
        this.target = target;
    }
}
