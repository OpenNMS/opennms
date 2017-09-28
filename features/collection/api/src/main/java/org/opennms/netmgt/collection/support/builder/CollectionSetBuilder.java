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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.MultiResourceCollectionSet;

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

    private final CollectionAgent m_agent;
    private CollectionStatus m_status = CollectionStatus.SUCCEEDED;
    private Date m_timestamp = new Date();
    private Map<Resource, List<Attribute<?>>> m_attributesByResource = new LinkedHashMap<>();

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

    public CollectionSetBuilder withNumericAttribute(Resource resource, String group, String name, Number value, AttributeType type) {
        return withAttribute(new NumericAttribute(resource, group, name, value, type));
    }

    public CollectionSetBuilder withStringAttribute(Resource resource, String group, String name, String value) {
        return withAttribute(new StringAttribute(resource, group, name, value));
    }

    private CollectionSetBuilder withAttribute(Attribute<?> attribute) {
        if (m_attributesByResource.containsKey(attribute.getResource())) {
            // Insert
            m_attributesByResource.get(attribute.getResource()).add(attribute);
        } else {
            // Append
            List<Attribute<?>> attributes = new ArrayList<>();
            attributes.add(attribute);
            m_attributesByResource.put(attribute.getResource(), attributes);
        }
        return this;
    }

    public CollectionSet build() {
        MultiResourceCollectionSet<CollectionResource> collectionSet = new MultiResourceCollectionSet<CollectionResource>() {};
        collectionSet.setCollectionTimestamp(m_timestamp);
        collectionSet.setStatus(m_status.getCode());
        for (final Entry<Resource, List<Attribute<?>>> entry : m_attributesByResource.entrySet()) {
            final Resource resource = entry.getKey();
            final AbstractCollectionResource collectionResource = new AbstractCollectionResource(m_agent) {
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
                public Path getPath() {
                    return super.getPath().resolve(resource.getPath(this));
                }

                @Override
                public String toString() {
                    return String.format("Resource[%s]/Node[%d]", resource, m_agent.getNodeId());
                }
            };
    
            for (Attribute<?> attribute : entry.getValue()) {
                final AttributeGroupType groupType = new AttributeGroupType(attribute.getGroup(), AttributeGroupType.IF_TYPE_ALL);
                final AbstractCollectionAttributeType attributeType = new AbstractCollectionAttributeType(groupType) {
                    @Override
                    public String getType() {
                        return attribute.getType().getName();
                    }

                    @Override
                    public String getName() {
                        return attribute.getName();
                    }

                    @Override
                    public void storeAttribute(CollectionAttribute collectionAttribute, Persister persister) {
                        if (attribute.getType() == AttributeType.STRING) {
                            persister.persistStringAttribute(collectionAttribute);
                        } else {
                            persister.persistNumericAttribute(collectionAttribute);
                        }
                    }

                    @Override
                    public String toString() {
                        return String.format("AttributeType[%s]/type[%s]", getName(), getType());
                    }
                };

                collectionResource.addAttribute(new AbstractCollectionAttribute(attributeType, collectionResource) {
                    @Override
                    public String getMetricIdentifier() {
                        return attribute.getName();
                    }

                    @Override
                    public Number getNumericValue() {
                        return attribute.getNumericValue();
                    }

                    @Override
                    public String getStringValue() {
                        return attribute.getStringValue();
                    }

                    @Override
                    public String toString() {
                        return String.format("Attribute[%s:%s]", getMetricIdentifier(), attribute.getValue());
                    }
                });
            }
            collectionSet.getCollectionResources().add(collectionResource);
        }
        return collectionSet;
    }
}
