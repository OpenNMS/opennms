/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension to SnmpCollectionSet to create a CollectionSet with the CollectionsetBuilder. This is an immediate step
 * in order to refactor the SnmpCollertor to use the CollectionsetBuilder.
 */
public class SnmpCollectionSetRefactorDummy extends SnmpCollectionSet implements Collectable, CollectionSet {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSetRefactorDummy.class);

    SnmpCollectionSetRefactorDummy(SnmpCollectionAgent agent, OnmsSnmpCollection snmpCollection, LocationAwareSnmpClient client) {
        super(agent,  snmpCollection, client);
    }

    CollectionSet createCollectionSet() {
        CollectionSetBuilder builder = new CollectionSetBuilder(getAgent());
        builder.withTimestamp(getCollectionTimestamp());

        for (CollectionResource collectionResource : getResources()) {
            if(collectionResource instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) collectionResource;
                NodeLevelResource resource = new NodeLevelResource(nodeInfo.getNodeId());
                addGroupsToBuilder(builder, resource, nodeInfo.getGroups());
                resource.setTimestamp(getCollectionTimestamp());
            } else if(collectionResource instanceof IfInfo) {
                IfInfo ifInfo = (IfInfo) collectionResource;
                InterfaceLevelResource resource = new InterfaceLevelResource(new NodeLevelResource(ifInfo.getNodeId()), ifInfo.getAttributesMap().get("snmpifname"));
                addGroupsToBuilder(builder, resource, ifInfo.getGroups());
                resource.setTimestamp(getCollectionTimestamp());
            } else if(collectionResource instanceof GenericIndexResource) {
                GenericIndexResource genericResource = (GenericIndexResource) collectionResource;
                ResourceType resourceType = ((GenericIndexResourceType)genericResource.getResourceType()).getResourceType();
                GenericTypeResource resource = new GenericTypeResource(new NodeLevelResource(genericResource.getCollectionAgent().getNodeId())
                        ,resourceType
                        ,genericResource.getInstance());
                resource.setTimestamp(getCollectionTimestamp());
                addGroupsToBuilder(builder, resource, genericResource.getGroups());
            } else {
                // We don't do anything for AliasedResource as discussed with jesse - AliasedResource is not used currently
                LOG.warn("we don't support {}, will ignore it", collectionResource.getClass().getName());
            }
        }
        return builder.build();
    }

    private void addGroupsToBuilder(CollectionSetBuilder builder, Resource resource, Collection<AttributeGroup> groups){
        for(AttributeGroup group : groups){
            for(CollectionAttribute attribute : group.getAttributes()){

                String value;
                if(attribute.getAttributeType() instanceof HexStringAttributeType){
                    value = ((SnmpAttribute)attribute).getValue().toHexString();
                } else {
                    value = attribute.getStringValue();
                }
                builder.withAttribute(resource, group.getName(), attribute.getName(), value, attribute.getType());
            }
        }
    }

}
