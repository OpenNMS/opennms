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
