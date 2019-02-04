/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.collectors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.integration.api.v1.collectors.CollectionRequest;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.CollectorRequestBuilder;
import org.opennms.integration.api.v1.collectors.resource.AttributeBuilder;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetBuilder;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetResourceBuilder;
import org.opennms.integration.api.v1.collectors.resource.GenericTypeResource;
import org.opennms.integration.api.v1.collectors.resource.IpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.NodeResource;
import org.opennms.integration.api.v1.collectors.resource.NumericAttribute;
import org.opennms.integration.api.v1.collectors.resource.ResourceBuilder;
import org.opennms.integration.api.v1.collectors.resource.StringAttribute;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectorRequestBuilderImpl implements CollectorRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorRequestBuilderImpl.class);

    private final LocationAwareCollectorClient locationAwareCollectorClient;

    private final CollectionAgentFactory collectionAgentFactory;

    private final NodeDao nodeDao;

    public CollectorRequestBuilderImpl(LocationAwareCollectorClient locationAwareCollectorClient, CollectionAgentFactory collectionAgentFactory, NodeDao nodeDao) {
        this.locationAwareCollectorClient = locationAwareCollectorClient;
        this.collectionAgentFactory = collectionAgentFactory;
        this.nodeDao = nodeDao;
    }

    private CollectionRequest collectionRequest;

    private String className;

    private Long ttlInMs;

    private Map<String, Object> attributes = new HashMap<>();

    @Override
    public CollectorRequestBuilder withRequest(CollectionRequest request) {
        this.collectionRequest = request;
        return this;
    }

    @Override
    public CollectorRequestBuilder withCollectorClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public CollectorRequestBuilder withTimeToLive(Long ttlInMs) {
        this.ttlInMs = ttlInMs;
        return this;
    }

    @Override
    public CollectorRequestBuilder withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public CollectorRequestBuilder withAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public CompletableFuture<CollectionSet> execute() {
        org.opennms.netmgt.collection.api.CollectionAgent collectionAgent =
                collectionAgentFactory.createCollectionAgent(collectionRequest.getNodeCriteria(), collectionRequest.getAddress());
        CompletableFuture<org.opennms.netmgt.collection.api.CollectionSet> future = locationAwareCollectorClient.collect()
                .withAgent(collectionAgent)
                .withCollectorClassName(className)
                .withAttributes(attributes)
                .withTimeToLive(ttlInMs)
                .execute();
        CompletableFuture<CollectionSet> result = new CompletableFuture<>();
        try {
            org.opennms.netmgt.collection.api.CollectionSet collectionSet = future.get();
            CollectionSetBuilder builder = new CollectionSetBuilder();

            if (collectionSet.getStatus().equals(CollectionStatus.FAILED)) {
                CollectionSet collectionSetResult = builder.withTimeStamp(collectionSet.getCollectionTimestamp().getTime())
                        .withStatus(CollectionSet.Status.FAILED).build();
                result.complete(collectionSetResult);
            } else {
                CollectionSet collectionSetResult = buildCollectionSet(builder, collectionSet);
                result.complete(collectionSetResult);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Collection encountered error ", e);
            result.completeExceptionally(e);
        }
        return result;
    }

    private CollectionSet buildCollectionSet(CollectionSetBuilder builder, org.opennms.netmgt.collection.api.CollectionSet collectionSet) {

        collectionSet.visit(new CollectionSetVisitor() {
            CollectionSetResourceBuilder resourceBuilder = new CollectionSetResourceBuilder();
            String groupName = null;

            @Override
            public void visitCollectionSet(org.opennms.netmgt.collection.api.CollectionSet set) {

            }

            @Override
            public void visitResource(CollectionResource resource) {
                if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_NODE)) {
                    resourceBuilder.withResource(buildNodeResource(resource));
                } else if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_IF)) {
                    IpInterfaceResource ipInterfaceResource = new ResourceBuilder()
                            .withInstance(resource.getInstance())
                            .buildIpInterfaceResource(buildNodeResource(resource));
                    resourceBuilder.withResource(ipInterfaceResource);
                } else {
                    GenericTypeResource genericTypeResource = new ResourceBuilder()
                            .withInstance(resource.getInstance())
                            .withType(resource.getResourceTypeName())
                            .buildGenericTypeResource(buildNodeResource(resource));
                    resourceBuilder.withResource(genericTypeResource);
                }
            }

            @Override
            public void visitGroup(AttributeGroup group) {
                groupName = group.getName();
            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                if (attribute.getType().equals(AttributeType.STRING)) {
                    StringAttribute stringAttribute = new AttributeBuilder()
                            .withName(attribute.getName())
                            .withStringValue(attribute.getStringValue())
                            .buildString();
                    resourceBuilder.withStringAttribute(stringAttribute);
                } else {
                    NumericAttribute numericAttribute = new AttributeBuilder()
                            .withName(attribute.getName())
                            .withGroup(groupName)
                            .withType((attribute.getType() == AttributeType.GAUGE) ? NumericAttribute.Type.GAUGE : NumericAttribute.Type.COUNTER)
                            .withNumericValue(attribute.getNumericValue().longValue())
                            .buildNumeric();
                    resourceBuilder.withNumericAttribute(numericAttribute);
                }
            }

            @Override
            public void completeAttribute(CollectionAttribute attribute) {

            }

            @Override
            public void completeGroup(AttributeGroup group) {

            }

            @Override
            public void completeResource(CollectionResource resource) {
                builder.withCollectionSetResource(resourceBuilder.build());
            }

            @Override
            public void completeCollectionSet(org.opennms.netmgt.collection.api.CollectionSet set) {
                builder.withTimeStamp(set.getCollectionTimestamp().getTime());
                builder.withStatus(CollectionSet.Status.SUCCEEDED);
            }
        });
        return builder.build();
    }

    private NodeResource buildNodeResource(CollectionResource collectionResource) {
        String nodeCriteria = getNodeCriteriaFromResource(collectionResource);
        Node node = nodeDao.getNodeByCriteria(nodeCriteria);
        NodeResource nodeResource = new ResourceBuilder()
                .withNodeId(node.getId())
                .withForeignId(node.getForeignId())
                .withForeignSource(node.getForeignSource())
                .withNodeLabel(node.getLabel())
                .buildNodeResource();
        return nodeResource;
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

    private boolean checkNumeric(String nodeCriteria) {
        try {
            Integer.parseInt(nodeCriteria);
            return true;
        } catch (NumberFormatException e) {
            // not a number
            return false;
        }
    }
}
