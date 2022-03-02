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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONObject;
import org.opennms.netmgt.graph.rest.api.PropertyConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.FallbackConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.IpInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.NodeInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.PrimitiveConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.StatusInfoConverter;
import org.opennms.netmgt.graph.rest.impl.converter.json.VertexRefConverter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class JsonPropertyConverterService {

    private final BundleContext bundleContext;

    public JsonPropertyConverterService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    public JSONObject convert(Map<String, Object> properties) {
        final JSONObject jsonObject = new JSONObject();
        if (!properties.isEmpty()) {
            final List<PropertyConverter> propertyConverters = getConverters();
            for (Map.Entry<String, Object> eachProperty : properties.entrySet()) {
                final Object value = eachProperty.getValue();
                if (value != null) {
                    final Class type = value.getClass();
                    final Optional<PropertyConverter> first = propertyConverters.stream().filter(converter -> converter.canConvert(type)).findFirst();
                    if (first.isPresent()) {
                        final Object convertedValue = first.get().convert(value);
                        jsonObject.put(eachProperty.getKey(), convertedValue);
                    } else {
                        LoggerFactory.getLogger(getClass()).warn("Cannot convert property of type {}. Skipping property {}", type, eachProperty.getKey());
                    }
                }
            }
        }
        return jsonObject;
    }

    private List<PropertyConverter> getConverters() {
        final List<PropertyConverter> propertyConverters = Lists.newArrayList();
        propertyConverters.add(new PrimitiveConverter());
        propertyConverters.add(new NodeInfoConverter());
        propertyConverters.add(new IpInfoConverter());
        propertyConverters.add(new VertexRefConverter());
        propertyConverters.add(new StatusInfoConverter());
        try {
            final Collection<ServiceReference<PropertyConverter>> serviceReferences = bundleContext.getServiceReferences(PropertyConverter.class, null);
            if (serviceReferences != null) {
                for (ServiceReference<PropertyConverter> serviceReference : serviceReferences) {
                    final PropertyConverter converter = bundleContext.getService(serviceReference);
                    propertyConverters.add(converter);
                }
            }
        } catch (InvalidSyntaxException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot fetch services due to wrong filter criteria", e);
        }
        propertyConverters.add(new FallbackConverter()); // Ensure this is always last entry in list
        return propertyConverters;
    }
}
