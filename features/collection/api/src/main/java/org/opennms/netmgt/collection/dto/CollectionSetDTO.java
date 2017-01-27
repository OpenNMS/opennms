/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.dto;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.builder.Attribute;
import org.opennms.netmgt.collection.support.builder.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.Resource;

@XmlRootElement(name = "collection-set")
@XmlAccessorType(XmlAccessType.NONE)
public class CollectionSetDTO implements CollectionSet {

    @XmlElement(name="agent")
    private CollectionAgentDTO agent;

    @XmlAttribute(name="status")
    private CollectionStatus status;

    @XmlAttribute(name="timestamp")
    private Date timestamp;

    @XmlElement(name="collection-resource")
    private List<CollectionResourceDTO> collectionResources;

    public CollectionSetDTO() { }

    public CollectionSetDTO(CollectionAgent agent, CollectionStatus status,
            Date timestamp, Map<Resource, List<Attribute<?>>> attributesByResource) {
        this.agent = new CollectionAgentDTO(agent);
        this.status = status;
        this.timestamp = timestamp;
        collectionResources = new ArrayList<>();
        for (Entry<Resource, List<Attribute<?>>> entry : attributesByResource.entrySet()) {
            collectionResources.add(new CollectionResourceDTO(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public String toString() {
        return String.format("CollectionSetDTO[agent=%s, collectionResources=%s, status=%s, timestamp=%s]",
                agent, collectionResources, status, timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agent, collectionResources, status, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof CollectionSetDTO)) {
            return false;
        }
        CollectionSetDTO other = (CollectionSetDTO) obj;
        return Objects.equals(this.agent, other.agent)
               && Objects.equals(this.collectionResources, other.collectionResources)
               && Objects.equals(this.status, other.status)
               && Objects.equals(this.timestamp, other.timestamp);
    }

    @Override
    public int getStatus() {
        return status.getCode();
    }

    @Override
    public boolean ignorePersist() {
        return false;
    }

    @Override
    public Date getCollectionTimestamp() {
        return timestamp;
    }

    private Set<CollectionResource> buildCollectionResources() {
        final Set<CollectionResource> collectionResources = new LinkedHashSet<>();
        for (CollectionResourceDTO entry : this.collectionResources) {
            final Resource resource = entry.getResource();
            final AbstractCollectionResource collectionResource = new AbstractCollectionResource(agent) {
                @Override
                public String getResourceTypeName() {
                    return "*";
                }

                @Override
                public String getInstance() {
                    return resource.getInstance();
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

            for (Attribute<?> attribute : entry.getAttributes()) {
                final AttributeGroupType groupType = new AttributeGroupType(attribute.getGroup(), AttributeGroupType.IF_TYPE_ALL);
                final AbstractCollectionAttributeType attributeType = new AbstractCollectionAttributeType(groupType) {
                    @Override
                    public AttributeType getType() {
                        return attribute.getType();
                    }

                    @Override
                    public String getName() {
                        return attribute.getName();
                    }

                    @Override
                    public void storeAttribute(CollectionAttribute collectionAttribute, Persister persister) {
                        if (AttributeType.STRING.equals(attribute.getType())) {
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
            collectionResources.add(collectionResource);
        }
        return collectionResources;
    }

    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for(CollectionResource resource : buildCollectionResources()) {
            resource.visit(visitor);
        }

        visitor.completeCollectionSet(this);
    }
}
