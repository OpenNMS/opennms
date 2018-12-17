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

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;

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


        // TODO Patrick Very ugly mapping until refactoring is done. We convert here the snmp world into the CollectionSetBuilder world
        for (CollectionResource collectionResource : getResources()) {
            if(collectionResource instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) collectionResource;
                NodeLevelResource resource = new NodeLevelResource(nodeInfo.getNodeId());
                addGroupsToBuilder(builder, resource, nodeInfo.getGroups());
            } else if(collectionResource instanceof IfInfo) {
                IfInfo ifInfo = (IfInfo) collectionResource;
                InterfaceLevelResource resource = new InterfaceLevelResource(new NodeLevelResource(ifInfo.getNodeId()), ifInfo.getAttributesMap().get("snmpifname"));
                addGroupsToBuilder(builder, resource, ifInfo.getGroups());
            } else if(collectionResource instanceof AliasedResource) {
                AliasedResource aliasedResource = (AliasedResource) collectionResource;
                Resource resource = null; // TODO: Patrick there is no AliasedResource in the CollectionSetBuilder world, what shall we do?
                addGroupsToBuilder(builder, resource, aliasedResource.getGroups());
            } else if(collectionResource instanceof GenericIndexResource) {
                GenericIndexResource genericResource = (GenericIndexResource) collectionResource;
                PersistenceSelectorStrategy persistenceSelectorStrategy = ((GenericIndexResourceType)genericResource.getResourceType()).getPersistenceSelectorStrategy();
                StorageStrategy storageStrategy = ((GenericIndexResourceType)genericResource.getResourceType()).getStorageStrategy();
                org.opennms.netmgt.config.datacollection.ResourceType resourceType = new org.opennms.netmgt.config.datacollection.ResourceType();
                resourceType.setName(genericResource.getResourceTypeName());
                resourceType.setLabel(genericResource.getResourceTypeName());
                resourceType.setPersistenceSelectorStrategy(convert(persistenceSelectorStrategy));
                // TODO Patrick what shall we set here?  resourceType.setResourceLabel();
                resourceType.setStorageStrategy(convert(storageStrategy));


//                DeferredGenericTypeResource resource = new DeferredGenericTypeResource(
//                        new NodeLevelResource(genericResource.getCollectionAgent().getNodeId()),
//                        genericResource.getResourceTypeName(),
//                        genericResource.getInstance());
                GenericTypeResource resource = new GenericTypeResource(new NodeLevelResource(genericResource.getCollectionAgent().getNodeId()),resourceType,genericResource.getInstance());
                addGroupsToBuilder(builder, resource, genericResource.getGroups());
            } else {
                throw new IllegalArgumentException("Unknown Resource: " + collectionResource.getClass().getName());
            }
        }
        return builder.build();
    }

    org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy convert(PersistenceSelectorStrategy strategy) {
        org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy persistenceSelectorStrategy = new org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy();
        persistenceSelectorStrategy.setClazz(strategy.getClass().getName());
        // TODO: patrick persistenceSelectorStrategy.setParameters(strategy.);
        return persistenceSelectorStrategy;
    }

    org.opennms.netmgt.config.datacollection.StorageStrategy convert(StorageStrategy strategy) {
        org.opennms.netmgt.config.datacollection.StorageStrategy storageStrategy = new org.opennms.netmgt.config.datacollection.StorageStrategy();
        storageStrategy.setClazz(strategy.getClass().getName());
        // TODO: patrick persistenceSelectorStrategy.setParameters(strategy.);
        return storageStrategy;
    }

    private void addGroupsToBuilder(CollectionSetBuilder builder, Resource resource, Collection<AttributeGroup> groups){
        for(AttributeGroup group : groups){
            for(CollectionAttribute attribute : group.getAttributes()){
                builder.withAttribute(resource, group.getName(), attribute.getName(), attribute.getStringValue(), attribute.getType());
            }
        }
    }

}
