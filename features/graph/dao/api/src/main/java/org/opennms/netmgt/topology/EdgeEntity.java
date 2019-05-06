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

import java.util.Objects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("edge")
public class EdgeEntity extends AbstractGraphEntity {

    // TODO: Patrick: discuss with Markus if we want to have source and target as properties?
    // Advantage: we can use the current implementation for storage.
    // Disadvantage: we need to filter out the source/target properties when converting to GenericEdge since these are
    // there object attributes not properties (and should be since an edge is defined by them)
    // sourceVertexRef als Embeddable VertexRef speichern

    public final static String PROPERTY_SOURCE = "sourceVertexRef";
    public final static String PROPERTY_TARGET = "targetVertexRef";

    public VertexRefEntity getSource() {
        return new VertexRefEntity(getPropertyValue(PROPERTY_SOURCE));
    }

    public void setSource(String namespace, String id) {
        setSource(new VertexRefEntity(namespace, id));
    }

    public void setSource(VertexRefEntity source) {
        Objects.requireNonNull(source, "source can not be null");
        this.setProperty(PROPERTY_SOURCE, String.class, source.asStringRepresentation());
    }

    public VertexRefEntity getTarget() {
        return new VertexRefEntity(getPropertyValue(PROPERTY_TARGET));
    }

    public void setTarget(String namespace, String id) {
        setTarget(new VertexRefEntity(namespace, id));
    }

    public void setTarget(VertexRefEntity target) {
        Objects.requireNonNull(target, "target can not be null");
        this.setProperty(PROPERTY_TARGET, String.class, target.toString());
    }
}
