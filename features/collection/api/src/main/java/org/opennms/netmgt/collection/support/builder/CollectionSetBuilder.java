/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.ConstantTimeKeeper;
import org.opennms.netmgt.collection.support.NumericAttributeUtils;
import org.opennms.netmgt.model.ResourcePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fluent API for building a {@link CollectionSet}.
 *
 * Many of the existing collectors end up implementing their collections,
 * attributes and resource types. This class is intended to replace those
 * and provide a common means for building the collection sets.
 *
 * @author jwhite
 */
public class CollectionSetBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionSetBuilder.class);

    private final CollectionAgent m_agent;
    private CollectionStatus m_status = CollectionStatus.SUCCEEDED;
    private Date m_timestamp = new Date();
    private Map<Resource, List<Attribute<?>>> m_attributesByResource = new LinkedHashMap<>();
    private boolean m_disableCounterPersistence = false;

    public CollectionSetBuilder(CollectionAgent agent) {
        m_agent = Objects.requireNonNull(agent, "agent cannot be null");
    }

    public CollectionSetBuilder withStatus(CollectionStatus status) {
        m_status = Objects.requireNonNull(status, "status cannot be null");
        return this;
    }

    public CollectionSetBuilder withTimestamp(Date timestamp) {
        m_timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        return this;
    }

    public CollectionSetBuilder withGauge(Resource resource, String group, String name, Number value) {
        return withAttribute(resource, new NumericAttribute(group, name, value, AttributeType.GAUGE, null));
    }

    public CollectionSetBuilder withCounter(Resource resource, String group, String name, Number value) {
        return withAttribute(resource, new NumericAttribute(group, name, value, AttributeType.COUNTER, null));
    }

    public CollectionSetBuilder withNumericAttribute(Resource resource, String group, String name, Number value, AttributeType type) {
        return withAttribute(resource, new NumericAttribute(group, name, value, type, null));
    }

    public CollectionSetBuilder withStringAttribute(Resource resource, String group, String name, String value) {
        return withAttribute(resource, new StringAttribute(group, name, value, null));
    }

    public CollectionSetBuilder withIdentifiedNumericAttribute(Resource resource, String group, String name, Number value, AttributeType type, String metricId) {
        return withAttribute(resource, new NumericAttribute(group, name, value, type, metricId));
    }

    public CollectionSetBuilder withIdentifiedStringAttribute(Resource resource, String group, String name, String value, String metricId) {
        return withAttribute(resource, new StringAttribute(group, name, value, metricId));
    }

    public CollectionSetBuilder withAttribute(Resource resource, String group, String name, String value, AttributeType type) {
        if (value == null) {
            LOG.info("Ignoring null value for attribute '{}' in group '{}' on resource '{}'", name, group, resource);
            return this;
        } else if (type.isNumeric()) {
            return withNumericAttribute(resource, group, name, NumericAttributeUtils.parseNumericValue(value), type);
        } else {
            return withStringAttribute(resource, group, name, value);
        }
    }

    private CollectionSetBuilder withAttribute(Resource resource, Attribute<?> attribute) {
        if (m_attributesByResource.containsKey(resource)) {
            // Insert
            m_attributesByResource.get(resource).add(attribute);
        } else {
            // Append
            List<Attribute<?>> attributes = new ArrayList<>();
            attributes.add(attribute);
            m_attributesByResource.put(resource, attributes);
        }
        return this;
    }

    public CollectionSetBuilder disableCounterPersistence(boolean disableCounterPersistence) {
        m_disableCounterPersistence = disableCounterPersistence;
        return this;
    }

    public CollectionSetDTO build() {
        return new CollectionSetDTO(m_agent, m_status, m_timestamp, m_attributesByResource, m_disableCounterPersistence);
    }

    public static AbstractCollectionResource toCollectionResource(Resource resource, CollectionAgent agent) {
        return new AbstractCollectionResource(agent) {
            private String label;

            @Override
            public String getResourceTypeName() {
                return resource.getTypeName();
            }

            @Override
            public String getInstance() {
                return resource.getInstance();
            }

            @Override
            public synchronized String getInterfaceLabel() {
                if (label == null) {
                    // Cache the results, since the operation may be expensive
                    label = resource.getLabel(this);
                }
                return label;
            }

            @Override
            public ResourcePath getPath() {
                return ResourcePath.get(super.getPath(), resource.getPath(this));
            }

            @Override
            public TimeKeeper getTimeKeeper() {
                if (resource.getTimestamp() != null) {
                    return new ConstantTimeKeeper(resource.getTimestamp());
                }
                return null;
            }

            @Override
            public String toString() {
                return resource.toString();
            }
        };
    }

    public int getNumResources() {
        return m_attributesByResource.keySet().size();
    }

    public int getNumAttributes() {
        return m_attributesByResource.values().stream()
                    .mapToInt(attrs -> attrs.size())
                    .sum();
    }
}
