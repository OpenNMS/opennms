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

import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("edge")
public class EdgeEntity extends AbstractGraphEntity {

    private final static String PROPERTY_SOURCE = "sourceVertexRef";
    private final static String PROPERTY_TARGET = "targetVertexRef";

    public VertexRef getSource() {
        return Optional.ofNullable(getPropertyValue(PROPERTY_SOURCE)).map(VertexRef::new).orElse(null);
    }

    public void setSource(String namespace, String id) {
        setSource(new VertexRef(namespace, id));
    }

    public void setSource(VertexRef source) {
        this.setProperty(PROPERTY_SOURCE, String.class, source.toString());
    }

    public VertexRef getTarget() {
        return Optional.ofNullable(getPropertyValue(PROPERTY_TARGET)).map(VertexRef::new).orElse(null);
    }

    public void setTarget(String namespace, String id) {
        setTarget(new VertexRef(namespace, id));
    }

    public void setTarget(VertexRef target) {
        this.setProperty(PROPERTY_SOURCE, String.class, target.toString());
    }
}
