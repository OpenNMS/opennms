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

import org.opennms.integration.api.v1.collectors.CollectionRequest;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.CollectorRequestBuilder;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableCollectionSet;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectorRequestBuilderImpl implements CollectorRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorRequestBuilderImpl.class);

    private final LocationAwareCollectorClient locationAwareCollectorClient;

    private final CollectionAgentFactory collectionAgentFactory;

    private final NodeDao nodeDao;

    private CollectionRequest collectionRequest;

    private String className;

    private Long ttlInMs;

    private Map<String, Object> attributes = new HashMap<>();

    public CollectorRequestBuilderImpl(LocationAwareCollectorClient locationAwareCollectorClient,
                                       CollectionAgentFactory collectionAgentFactory,
                                       NodeDao nodeDao) {
        this.locationAwareCollectorClient = locationAwareCollectorClient;
        this.collectionAgentFactory = collectionAgentFactory;
        this.nodeDao = nodeDao;
    }

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
        CompletableFuture<org.opennms.netmgt.collection.api.CollectionSet> result = locationAwareCollectorClient.collect()
                .withAgent(collectionAgent)
                .withCollectorClassName(className)
                .withAttributes(attributes)
                .withTimeToLive(ttlInMs)
                .execute();
        CompletableFuture<CollectionSet> future = new CompletableFuture<>();
        try {
            org.opennms.netmgt.collection.api.CollectionSet collectionSet = result.get();
            ImmutableCollectionSet.Builder builder = ImmutableCollectionSet.newBuilder();
            if (collectionSet.getStatus().equals(CollectionStatus.FAILED)) {
                CollectionSet collectionSetResult = builder.setTimestamp(collectionSet.getCollectionTimestamp().getTime())
                        .setStatus(CollectionSet.Status.FAILED).build();
                future.complete(collectionSetResult);
            } else {
                CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao);
                future.complete(collectionSetMapper.buildCollectionSet(builder, collectionSet));
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

}
