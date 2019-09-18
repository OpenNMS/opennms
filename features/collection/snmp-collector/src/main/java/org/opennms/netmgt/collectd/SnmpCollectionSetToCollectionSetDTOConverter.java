/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Objects;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.AbstractResource;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Converts a {@link SnmpCollectionSet} to {@link CollectionSetDTO}. This is an immediate step
 * in order to refactor the SnmpCollector to use the {@link CollectionSetBuilder}.
 * </p>
 * <p>
 * Usage:
 * <code>new SnmpCollectionSetToCollectionSetDTOConverter().withParameters(params).convert(collectionSet)</code>
 * </p>
 * 
 */
public class SnmpCollectionSetToCollectionSetDTOConverter {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSetToCollectionSetDTOConverter.class);

    private ServiceParameters params;

    public SnmpCollectionSetToCollectionSetDTOConverter withParameters(ServiceParameters params) {
        this.params = params;
        return this;
    }

    public CollectionSetDTO convert(SnmpCollectionSet collectionSet) {
        Objects.requireNonNull(collectionSet, "SnmpCollectionSet cannot be null");
        Objects.requireNonNull(params, "ServiceParameters cannot be null");
        CollectionSetBuilder builder = new CollectionSetBuilder(collectionSet.getAgent());
        builder.withTimestamp(collectionSet.getCollectionTimestamp());

        for (CollectionResource collectionResource : collectionSet.getResources()){
            addResourceToBuilder(builder, collectionResource);
        }
        return builder.build();
    }

    public void addResourceToBuilder(CollectionSetBuilder builder, CollectionResource collectionResource) {

            SnmpCollectionResource snmpResource = (SnmpCollectionResource) collectionResource;
            AbstractResource builderResource;

            if(collectionResource instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) collectionResource;
                builderResource = new NodeLevelResource(nodeInfo.getNodeId());
            } else if(collectionResource instanceof IfInfo) {
                IfInfo ifInfo = (IfInfo) collectionResource;
                builderResource = new InterfaceLevelResource(new NodeLevelResource(ifInfo.getNodeId()), ifInfo.getAttributesMap().get("snmpifname"));
            } else if(collectionResource instanceof GenericIndexResource) {
                GenericIndexResource genericResource = (GenericIndexResource) collectionResource;
                ResourceType resourceType = ((GenericIndexResourceType)genericResource.getResourceType()).getResourceType();
                builderResource = new GenericTypeResource(new NodeLevelResource(genericResource.getCollectionAgent().getNodeId())
                        ,resourceType
                        ,genericResource.getUnmodifiedInstance());
            } else {
                // We don't do anything for AliasedResource as discussed with jesse - AliasedResource is not used currently
                LOG.warn("we don't support {}, will ignore it", collectionResource.getClass().getName());
                return;
            }
            // we add only groups that should be persisted to the CollectionBuilder since it doesn't have the concept
            // of "shouldPersist".
            Collection<AttributeGroup> groups = snmpResource.getGroups();
            if(collectionResource.shouldPersist(params)) {
                addGroupsToBuilder(builder, builderResource, groups);
            }
    }

    private void addGroupsToBuilder(CollectionSetBuilder builder, Resource resource, Collection<AttributeGroup> groups){
        for(AttributeGroup group : groups){
            if(group.shouldPersist(params)){
                addGroupToBuilder(builder, resource, group);
            }
        }
    }

    private void addGroupToBuilder(CollectionSetBuilder builder, Resource resource, AttributeGroup group) {
        for (CollectionAttribute attribute : group.getAttributes()) {
            String value;
            if(!attribute.shouldPersist(params)){
                continue;
            } else if (attribute.getAttributeType() instanceof HexStringAttributeType) {
                value = ((SnmpAttribute) attribute).getValue().toHexString();
            } else {
                value = attribute.getStringValue();
            }
            builder.withAttribute(resource, group.getName(), attribute.getName(), value, attribute.getType());
        }
    }
}
