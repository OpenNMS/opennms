/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.apilayer.collectors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.features.apilayer.common.collectors.CollectionSetMapper;
import org.opennms.integration.api.v1.collectors.CollectionRequest;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.CollectorRequestBuilder;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableCollectionSet;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;

public class CollectorRequestBuilderImpl implements CollectorRequestBuilder {

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
                collectionAgentFactory.createCollectionAgent(Integer.toString(collectionRequest.getNodeId()), collectionRequest.getAddress());
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
