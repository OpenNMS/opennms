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

package org.opennms.netmgt.graph.rest.impl.converter;

import java.util.Objects;

import org.json.JSONObject;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.IpInfo;
import org.opennms.netmgt.graph.rest.impl.converter.json.GraphContainerConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.GraphContainerInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.GraphConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.IpInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.VertexConverter;
import org.osgi.framework.BundleContext;

public class JsonConverterService {

    private final BundleContext bundleContext;

    public JsonConverterService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public JSONObject convert(GraphContainerInfo containerInfo) {
        return new GraphContainerInfoConverter().convert(containerInfo);
    }

    public JSONObject convert(ImmutableGraphContainer<?> graphContainer) {
        return new GraphContainerConverter(bundleContext).convert(graphContainer);
    }

    public JSONObject convert(ImmutableGraph<?, ?> graph) {
        return new GraphConverter(bundleContext).convert(graph);
    }

    public JSONObject convert(Vertex vertex) {
        return new VertexConverter(bundleContext).convert(vertex);
    }

    public JSONObject convert(IpInfo ipInfo) {
        return new IpInfoConverter().convert(ipInfo);
    }
}
