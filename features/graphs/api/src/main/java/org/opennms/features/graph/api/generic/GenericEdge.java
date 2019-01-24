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

package org.opennms.features.graph.api.generic;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.graph.api.AbstractEdge;
import org.opennms.features.graph.api.VertexRef;

public class GenericEdge extends AbstractEdge<GenericVertex> {

    private Map<String, Object> properties = new HashMap<>();

    public GenericEdge(GenericVertex source, GenericVertex target) {
        this((VertexRef) source, (VertexRef) target);
    }

    public GenericEdge(VertexRef source, VertexRef target) {
        super(source, target);
        properties.put(GenericProperties.LABEL, getLabel());
        properties.put(GenericProperties.NAMESPACE, getNamespace());
        properties.put(GenericProperties.ID, getId());
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}
