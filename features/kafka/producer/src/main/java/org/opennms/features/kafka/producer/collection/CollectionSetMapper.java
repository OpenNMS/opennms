/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.collection;

import java.util.Objects;

import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.features.kafka.producer.model.CollectionSetProtos.NumericAttribute.Type;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

public class CollectionSetMapper {

    @Autowired
    private NodeDao nodeDao;
    
    private final TransactionOperations transactionOperations;
    
    public CollectionSetMapper(NodeDao nodeDao, TransactionOperations transactionOperations) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    public CollectionSetProtos.CollectionSet buildCollectionSetProtos(CollectionSet collectionSet) {
        CollectionSetProtos.CollectionSet.Builder builder = CollectionSetProtos.CollectionSet.newBuilder();

        collectionSet.visit(new CollectionSetVisitor() {
            CollectionSetProtos.CollectionSetResource.Builder collectionSetResourceBuilder;
            String lastGroupName = null;

            @Override
            public void visitCollectionSet(CollectionSet set) {

            }

            @Override
            public void visitResource(CollectionResource resource) {
                collectionSetResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_NODE)) {
                    String nodeCriteria = getNodeCriteriaFromResource(resource);
                    CollectionSetProtos.NodeLevelResource.Builder nodeResourceBuilder = buildNodeLevelResourceForProto(
                            nodeCriteria);
                    collectionSetResourceBuilder.setNode(nodeResourceBuilder);
                } else if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_IF)) {
                    CollectionSetProtos.InterfaceLevelResource.Builder interfaceResourceBuilder = CollectionSetProtos.InterfaceLevelResource
                            .newBuilder();
                    String nodeCriteria = getNodeCriteriaFromResource(resource);
                    CollectionSetProtos.NodeLevelResource.Builder nodeResourceBuilder = buildNodeLevelResourceForProto(
                            nodeCriteria);
                    interfaceResourceBuilder.setNode(nodeResourceBuilder);
                    interfaceResourceBuilder.setInstance(resource.getInterfaceLabel());
                    collectionSetResourceBuilder.setInterface(interfaceResourceBuilder);
                } else {
                    CollectionSetProtos.GenericTypeResource.Builder genericResourceBuilder = CollectionSetProtos.GenericTypeResource
                            .newBuilder();
                    String nodeCriteria = getNodeCriteriaFromResource(resource);
                    CollectionSetProtos.NodeLevelResource.Builder nodeResourceBuilder = buildNodeLevelResourceForProto(
                            nodeCriteria);
                    genericResourceBuilder.setNode(nodeResourceBuilder);
                    genericResourceBuilder.setType(resource.getResourceTypeName());
                    genericResourceBuilder.setInstance(resource.getInstance());
                    collectionSetResourceBuilder.setGeneric(genericResourceBuilder);
                }
            }

            @Override
            public void visitGroup(AttributeGroup group) {
                lastGroupName = group.getName();

            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                if (attribute.getType().equals(AttributeType.STRING)) {
                    CollectionSetProtos.StringAttribute.Builder attributeBuilder = CollectionSetProtos.StringAttribute
                            .newBuilder();
                    attributeBuilder.setValue(attribute.getStringValue());
                    attributeBuilder.setName(attribute.getName());
                    collectionSetResourceBuilder.addString(attributeBuilder);
                } else {
                    CollectionSetProtos.NumericAttribute.Builder attributeBuilder = CollectionSetProtos.NumericAttribute
                            .newBuilder();
                    attributeBuilder.setGroup(lastGroupName);
                    attributeBuilder.setName(attribute.getName());
                    attributeBuilder.setValue(attribute.getNumericValue().longValue());
                    attributeBuilder.setType((attribute.getType() == AttributeType.GAUGE) ? Type.GAUGE : Type.COUNTER);
                    collectionSetResourceBuilder.addNumeric(attributeBuilder);
                }

            }

            @Override
            public void completeAttribute(CollectionAttribute attribute) {
                // we already set it in visitAttribute

            }

            @Override
            public void completeGroup(AttributeGroup group) {
                // group here is part of Numeric, it's already set in numeric

            }

            @Override
            public void completeResource(CollectionResource resource) {
                builder.addResource(collectionSetResourceBuilder);
            }

            @Override
            public void completeCollectionSet(CollectionSet set) {
                builder.setTimestamp(collectionSet.getCollectionTimestamp().getTime());
            }

        });

        return builder.build();
    }

    private String getNodeCriteriaFromResource(CollectionResource resource) {

        String[] parentResourcePathElements = null;
        if ( resource != null && resource.getParent() != null ) {
          parentResourcePathElements = resource.getParent().elements();
        } else {
            return null;
        }
        String nodeCriteria = null;
        if (ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(parentResourcePathElements[0])) {
            final String fs = parentResourcePathElements[1];
            final String fid = parentResourcePathElements[2];
            nodeCriteria = fs + ":" + fid;
        } else {
            final String nodeId = parentResourcePathElements[0];
            nodeCriteria = nodeId;
        }
        return nodeCriteria;
    }

    
    public CollectionSetProtos.NodeLevelResource.Builder buildNodeLevelResourceForProto(String nodeCriteria) {
        CollectionSetProtos.NodeLevelResource.Builder nodeResourceBuilder = CollectionSetProtos.NodeLevelResource
                .newBuilder();
        transactionOperations.execute((TransactionCallback<Void>) status -> {
            try {
                OnmsNode node = nodeDao.get(nodeCriteria);
                nodeResourceBuilder.setNodeId(node.getId());
                nodeResourceBuilder.setNodeLabel(node.getLabel());
                nodeResourceBuilder.setForeignId(node.getForeignId());
                nodeResourceBuilder.setForeignSource(node.getForeignSource());
                nodeResourceBuilder.setLocation(node.getLocation().getLocationName());
            } catch (Exception e) {
               //TODO: Deal with response time resources
            }
            return null;
        });
        return nodeResourceBuilder;
    }
}
