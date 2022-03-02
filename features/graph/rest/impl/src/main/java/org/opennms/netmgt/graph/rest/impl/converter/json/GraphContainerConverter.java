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

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.rest.api.Converter;
import org.opennms.netmgt.graph.rest.impl.converter.JsonPropertyConverterService;
import org.osgi.framework.BundleContext;

public class GraphContainerConverter implements Converter<ImmutableGraphContainer<?>, JSONObject> {

    private final BundleContext bundleContext;

    public GraphContainerConverter(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canConvert(Class<ImmutableGraphContainer<?>> type) {
        return ImmutableGraphContainer.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(ImmutableGraphContainer<?> input) {
        final JSONObject jsonContainer = new JSONObject();
        final JSONArray jsonGraphArray = new JSONArray();
        jsonContainer.put("graphs", jsonGraphArray);

        final GenericGraphContainer genericGraphContainer = input.asGenericGraphContainer();
        final Map<String, Object> properties = genericGraphContainer.getProperties();
        final JSONObject convertedProperties = new JsonPropertyConverterService(bundleContext).convert(properties);
        convertedProperties.toMap().forEach(jsonContainer::put);

        genericGraphContainer.getProperties().forEach((key, value) -> jsonContainer.put(key, value));
        input.getGraphs()
                .stream()
                .sorted(Comparator.comparing(GraphInfo::getNamespace))
                .forEach(graph -> {
                            final JSONObject jsonGraph = new GraphConverter(bundleContext).convert(graph);
                            jsonGraphArray.put(jsonGraph);
                        }
                );
        return jsonContainer;
    }
}
