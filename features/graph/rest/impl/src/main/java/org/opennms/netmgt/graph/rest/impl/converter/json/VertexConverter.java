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

package org.opennms.netmgt.graph.rest.impl.converter.json;

import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.rest.api.Converter;
import org.opennms.netmgt.graph.rest.impl.converter.JsonPropertyConverterService;
import org.osgi.framework.BundleContext;

public class VertexConverter implements Converter<Vertex, JSONObject> {

    private final BundleContext bundleContext;

    public VertexConverter(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canConvert(Class<Vertex> type) {
        return Vertex.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(Vertex input) {
        final GenericVertex genericVertex = input.asGenericVertex();
        final Map<String, Object> properties = genericVertex.getProperties();
        final JSONObject jsonVertex = new JsonPropertyConverterService(bundleContext).convert(properties);
        return jsonVertex;
    }
}
