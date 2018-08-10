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

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Strings;

public class CollectionSetMapper {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionSetMapper.class);

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
                    if (!Strings.isNullOrEmpty(nodeCriteria)) {
                        CollectionSetProtos.NodeLevelResource.Builder nodeResourceBuilder = buildNodeLevelResourceForProto(
                                nodeCriteria);
                        interfaceResourceBuilder.setNode(nodeResourceBuilder);
                        interfaceResourceBuilder.setInstance(resource.getInterfaceLabel());
                        collectionSetResourceBuilder.setInterface(interfaceResourceBuilder);
                    } else {
                        // it is likely to be response time resource.
                        CollectionSetProtos.ResponseTimeResource.Builder responseTimeResource = buildResponseTimeResource(
                                resource);
                        if (responseTimeResource != null) {
                            collectionSetResourceBuilder.setResponse(responseTimeResource);
                        }
                    }

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

        String nodeCriteria = null;
        if (resource.getParent() != null) {
            String[] resourcePathArray = resource.getParent().elements();
            if (ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(resourcePathArray[0])
                    && resourcePathArray.length == 3) {
                // parent denotes nodeCriteria, form fs:fid
                nodeCriteria = resourcePathArray[1] + ":" + resourcePathArray[2];
            } else if (checkNumeric(resourcePathArray[0])) {
                // parent denotes nodeId
                nodeCriteria = resourcePathArray[0];
            }
        }
        return nodeCriteria;
    }

    private CollectionSetProtos.ResponseTimeResource.Builder buildResponseTimeResource(CollectionResource resource) {
        boolean validIp = false;
        // Check if resource parent is an IpAddress.
        if (resource.getParent() != null && resource.getParent().elements().length == 1) {
            String[] resourcePathArray = resource.getParent().elements();
            validIp = checkForValidIpAddress(resourcePathArray[0]);
        }
        if (resource.getPath() != null && validIp) {
            // extract path which consists of location and IpAddress.
            String[] resourcePathArray = resource.getPath().elements();
            CollectionSetProtos.ResponseTimeResource.Builder responseTimeResourceBuilder = CollectionSetProtos.ResponseTimeResource
                    .newBuilder();
            if (resourcePathArray.length == 2) {
                // first element is location, 2nd IpAddress.
                responseTimeResourceBuilder.setLocation(resourcePathArray[0]);
                responseTimeResourceBuilder.setInstance(resourcePathArray[1]);
            } else if (resourcePathArray.length == 1) {
                responseTimeResourceBuilder.setInstance(resourcePathArray[0]);
            }
            return responseTimeResourceBuilder;
        }
        return null;
    }

    private boolean checkNumeric(String nodeCriteria) {
        try {
            Integer.parseInt(nodeCriteria);
            return true;
        } catch (NumberFormatException e) {
            // not a number
            return false;
        }
    }

    private boolean checkForValidIpAddress(String resourcePath) {
        try {
            InetAddress.getByName(resourcePath);
            return true;
        } catch (UnknownHostException e) {
            // not an ipaddress.
            return false;
        }
    }

    public CollectionSetProtos.NodeLevelResource.Builder buildNodeLevelResourceForProto(String nodeCriteria) {
        CollectionSetProtos.NodeLevelResource.Builder nodeResourceBuilder = CollectionSetProtos.NodeLevelResource
                .newBuilder();
        transactionOperations.execute((TransactionCallback<Void>) status -> {
            try {
                OnmsNode node = nodeDao.get(nodeCriteria);
                if (node != null) {
                    nodeResourceBuilder.setNodeId(node.getId());
                    nodeResourceBuilder.setNodeLabel(node.getLabel());
                    nodeResourceBuilder.setForeignId(node.getForeignId());
                    nodeResourceBuilder.setForeignSource(node.getForeignSource());
                    if (node.getLocation() != null) {
                        nodeResourceBuilder.setLocation(node.getLocation().getLocationName());
                    }
                }
            } catch (Exception e) {
                LOG.error("error while trying to match node from {}", nodeCriteria);
            }
            return null;
        });
        return nodeResourceBuilder;
    }
}
