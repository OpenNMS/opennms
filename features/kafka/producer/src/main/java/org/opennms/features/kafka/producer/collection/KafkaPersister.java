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
package org.opennms.features.kafka.producer.collection;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.features.kafka.producer.model.CollectionSetProtos.CollectionSetResource;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class KafkaPersister implements Persister {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersister.class);

    private static final int MAX_BUFFER_SIZE_CONFIGURED = 921600;

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private CollectionSetMapper collectionSetMapper;

    private final ServiceParameters m_params;

    private Producer<String, byte[]> producer;

    private String topicName = "metrics";

    private Boolean disableMetricsSplitting = false;

    private Expression metricFilterExpression;

    public KafkaPersister(ServiceParameters params) {
        m_params = params;
    }

    public KafkaPersister() {
        m_params = new ServiceParameters(Collections.emptyMap());
    }

    /** {@inheritDoc} */
    @Override
    public void visitCollectionSet(CollectionSet collectionSet) {
        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper
                .buildCollectionSetProtos(collectionSet, m_params);
        if (collectionSetProto != null) {
            // Apply filtering if configured
            CollectionSetProtos.CollectionSet filteredCollectionSetProto = applyMetricFilter(collectionSetProto);
            if (filteredCollectionSetProto != null && filteredCollectionSetProto.getResourceCount() > 0) {
                bisectAndSendMessageToKafka(filteredCollectionSetProto);
            }
        }
    }

    void bisectAndSendMessageToKafka(CollectionSetProtos.CollectionSet collectionSetProto) {
        if (!getDisableMetricsSplitting() && checkForMaxSize(collectionSetProto.toByteArray().length)) {
            if(collectionSetProto.getResourceCount() == 1) {
                /// Handle the case where resource is only one with too many attributes that can cross max buffer size.
                CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
                if(collectionSetResource.getNumericList().size() > 0) {
                    // Handle numeric attributes only.
                    CollectionSetProtos.CollectionSetResource.Builder numericResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                    numericResourceBuilder.mergeFrom(collectionSetResource).clearString();
                    CollectionSetProtos.CollectionSet collectionSetWithNumeric = CollectionSetProtos.CollectionSet.newBuilder()
                            .addResource(numericResourceBuilder).setTimestamp(collectionSetProto.getTimestamp()).build();
                    bisectNumericAttributes(collectionSetWithNumeric);
                }
                if(collectionSetResource.getStringList().size() > 0) {
                    // Handle string attributes only
                    CollectionSetProtos.CollectionSetResource.Builder stringResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                    stringResourceBuilder.mergeFrom(collectionSetResource).clearNumeric();
                    CollectionSetProtos.CollectionSet collectionSetWithStringAttributes = CollectionSetProtos.CollectionSet.newBuilder()
                            .addResource(stringResourceBuilder).setTimestamp(collectionSetProto.getTimestamp()).build();
                    bisectStringAttributes(collectionSetWithStringAttributes);
                }
            } else {
                // Divide resources into two in recursive way.
                Iterator<List<CollectionSetResource>> subList = Iterables.partition(collectionSetProto.getResourceList(),
                        (collectionSetProto.getResourceCount() + 1) / 2).iterator();

                CollectionSetProtos.CollectionSet firstPartCollectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                        .mergeFrom(collectionSetProto).clearResource().addAllResource(subList.next()).build();
                bisectAndSendMessageToKafka(firstPartCollectionSet);

                CollectionSetProtos.CollectionSet secondPartCollectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                        .mergeFrom(collectionSetProto).clearResource().addAllResource(subList.next()).build();
                bisectAndSendMessageToKafka(secondPartCollectionSet);
            }
        } else {
            sendMessageToKafka(collectionSetProto);
        }
    }

    private void bisectNumericAttributes(CollectionSetProtos.CollectionSet collectionSetProto) {
        // Divide numeric attributes into two in recursive way
        if (checkForMaxSize(collectionSetProto.toByteArray().length)) {
            Iterator<List<CollectionSetProtos.NumericAttribute>> subList = Iterables.partition(collectionSetProto.getResource(0).getNumericList(),
                    (collectionSetProto.getResource(0).getNumericCount() + 1) / 2).iterator();
            bisectNumericAttributes(buildCollectionSetWithNumericAttributes(collectionSetProto, subList.next()));
            bisectNumericAttributes(buildCollectionSetWithNumericAttributes(collectionSetProto, subList.next()));
        } else {
            sendMessageToKafka(collectionSetProto);
        }
    }

    private CollectionSetProtos.CollectionSet buildCollectionSetWithNumericAttributes(CollectionSetProtos.CollectionSet originalCollectionSet,
                                                                                      List<CollectionSetProtos.NumericAttribute> numericAttributes) {

        CollectionSetProtos.CollectionSet.Builder collectionSetBuilder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(originalCollectionSet.getTimestamp());
        CollectionSetProtos.CollectionSetResource.Builder collectionSetResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
        collectionSetResourceBuilder.mergeFrom(originalCollectionSet.getResource(0)).clearNumeric().addAllNumeric(numericAttributes);
        collectionSetBuilder.addResource(collectionSetResourceBuilder);
        return collectionSetBuilder.build();
    }

    private void bisectStringAttributes(CollectionSetProtos.CollectionSet collectionSetProto) {
        // Divide string attributes into two in recursive way
        if (checkForMaxSize(collectionSetProto.toByteArray().length)) {
            Iterator<List<CollectionSetProtos.StringAttribute>> subList = Iterables.partition(collectionSetProto.getResource(0).getStringList(),
                    (collectionSetProto.getResource(0).getStringCount() + 1) / 2).iterator();
            bisectStringAttributes(buildCollectionSetWithStringAttributes(collectionSetProto, subList.next()));
            bisectStringAttributes(buildCollectionSetWithStringAttributes(collectionSetProto, subList.next()));
        } else {
            sendMessageToKafka(collectionSetProto);
        }
    }

    private CollectionSetProtos.CollectionSet buildCollectionSetWithStringAttributes(CollectionSetProtos.CollectionSet originalCollectionSet,
                                                                                      List<CollectionSetProtos.StringAttribute> stringAttributes) {

        CollectionSetProtos.CollectionSet.Builder collectionSetBuilder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(originalCollectionSet.getTimestamp());
        CollectionSetProtos.CollectionSetResource.Builder collectionSetResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
        collectionSetResourceBuilder.mergeFrom(originalCollectionSet.getResource(0)).clearString().addAllString(stringAttributes);
        collectionSetBuilder.addResource(collectionSetResourceBuilder);
        return collectionSetBuilder.build();
    }

    boolean checkForMaxSize(int length) {
        return length > MAX_BUFFER_SIZE_CONFIGURED;
    }
    
    private void sendMessageToKafka( CollectionSetProtos.CollectionSet collectionSetProto) {
        // If no resources should be persisted, do not send an empty CollectionSet
        if (collectionSetProto.getResourceCount() == 0) {
            return;
        }
        // Derive key, it will be nodeId for all resources except for response time, it would be IpAddress
        final String key = deriveKeyFromCollectionSet(collectionSetProto);
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, key,
                collectionSetProto.toByteArray());
        producer.send(record, (recordMetadata, e) -> {
            if (e != null) {
                LOG.warn("Failed to send record to producer: {}.", record, e);
                return;
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("persisted collection {} to kafka with key {}", collectionSetProto.toString(), key);
            }
        });
    }

    private String deriveKeyFromCollectionSet(CollectionSetProtos.CollectionSet collectionSetProto) {
        String key = "";
        if (collectionSetProto.getResourceCount() > 0) {
            CollectionSetResource firstResource = collectionSetProto.getResource(0);
            if (firstResource.hasResponse()) {
                // For response time resources, key will be instance i.e. IpAddress
                key = firstResource.getResponse().getInstance();
            } else if (firstResource.hasInterface()) {
                key = Long.toString(firstResource.getInterface().getNode().getNodeId());
            } else if (firstResource.hasGeneric()) {
                key = Long.toString(firstResource.getGeneric().getNode().getNodeId());
            } else if (firstResource.hasNode()) {
                key = Long.toString(firstResource.getNode().getNodeId());
            } 
        }
        return key;
    }

    public void setTopicName(String topicName) {
        if (!Strings.isNullOrEmpty(topicName)) {
            this.topicName = topicName;
        }
    }

    public void setProducer(Producer<String, byte[]> producer) {
        this.producer = producer;
    }

    public void setCollectionSetMapper(CollectionSetMapper collectionSetMapper) {
        this.collectionSetMapper = collectionSetMapper;
    }

    @Override
    public void visitResource(CollectionResource resource) {
        // not handled here
    }

    @Override
    public void visitGroup(AttributeGroup group) {
        // not handled here
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    @Override
    public void completeGroup(AttributeGroup group) {
        // not handled here
    }

    @Override
    public void completeResource(CollectionResource resource) {
        // not handled here
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
        // not handled here
    }

    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        // not handled here
    }

    public void setDisableMetricsSplitting(Boolean disableMetricsSplitting) {
        this.disableMetricsSplitting = disableMetricsSplitting;
    }

    public Boolean getDisableMetricsSplitting() {
        return disableMetricsSplitting;
    }

    public void setMetricFilter(final String metricFilter) {
        if (Strings.isNullOrEmpty(metricFilter)) {
            metricFilterExpression = null;
        } else {
            metricFilterExpression = SPEL_PARSER.parseExpression(metricFilter);
        }
    }

    /**
     * Apply metric filter to the CollectionSet.
     * Filters resources based on the configured SpEL expression.
     * If no filter is configured, returns the original CollectionSet unchanged.
     */
    private CollectionSetProtos.CollectionSet applyMetricFilter(final CollectionSetProtos.CollectionSet collectionSet) {
        if (metricFilterExpression == null) {
            // No filter configured, return original
            return collectionSet;
        }

        // Filter resources based on the expression
        final CollectionSetProtos.CollectionSet.Builder filteredBuilder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(collectionSet.getTimestamp());

        for (CollectionSetProtos.CollectionSetResource resource : collectionSet.getResourceList()) {
            if (shouldForwardResource(resource)) {
                filteredBuilder.addResource(resource);
            }
        }

        return filteredBuilder.build();
    }

    /**
     * Evaluate whether a resource should be forwarded based on the metrics filter.
     */
    private boolean shouldForwardResource(final CollectionSetProtos.CollectionSetResource resource) {
        try {
            // Create a context object with accessible properties for SpEL evaluation
            final ResourceFilterContext context = new ResourceFilterContext(resource);
            final Boolean result = metricFilterExpression.getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            LOG.error("Metric filter '{}' failed to evaluate for resource. The resource will be forwarded anyways.", metricFilterExpression.getExpressionString(), e);
            return true; // Forward on error to avoid data loss
        }
    }

    /**
     * Context object that exposes resource properties for SpEL filtering.
     * Provides access to node properties (nodeId, nodeLabel, foreignSource, foreignId, location)
     * and resource properties (resourceType, resourceName).
     */
    public static class ResourceFilterContext {
        private final CollectionSetProtos.CollectionSetResource resource;

        public ResourceFilterContext(CollectionSetProtos.CollectionSetResource resource) {
            this.resource = resource;
        }

        public Long getNodeId() {
            if (resource.hasNode()) {
                return resource.getNode().getNodeId();
            } else if (resource.hasInterface() && resource.getInterface().hasNode()) {
                return resource.getInterface().getNode().getNodeId();
            } else if (resource.hasGeneric() && resource.getGeneric().hasNode()) {
                return resource.getGeneric().getNode().getNodeId();
            } else if (resource.hasResponse() && resource.getResponse().hasNode()) {
                return resource.getResponse().getNode().getNodeId();
            }
            return null;
        }

        public String getNodeLabel() {
            String nodeLabel = null;
            if (resource.hasNode() && !resource.getNode().getNodeLabel().isEmpty()) {
                nodeLabel = resource.getNode().getNodeLabel();
            } else if (resource.hasInterface() && resource.getInterface().hasNode() && !resource.getInterface().getNode().getNodeLabel().isEmpty()) {
                nodeLabel = resource.getInterface().getNode().getNodeLabel();
            } else if (resource.hasGeneric() && resource.getGeneric().hasNode() && !resource.getGeneric().getNode().getNodeLabel().isEmpty()) {
                nodeLabel = resource.getGeneric().getNode().getNodeLabel();
            } else if (resource.hasResponse() && resource.getResponse().hasNode() && !resource.getResponse().getNode().getNodeLabel().isEmpty()) {
                nodeLabel = resource.getResponse().getNode().getNodeLabel();
            }
            return nodeLabel;
        }

        public String getForeignSource() {
            String foreignSource = null;
            if (resource.hasNode() && !resource.getNode().getForeignSource().isEmpty()) {
                foreignSource = resource.getNode().getForeignSource();
            } else if (resource.hasInterface() && resource.getInterface().hasNode() && !resource.getInterface().getNode().getForeignSource().isEmpty()) {
                foreignSource = resource.getInterface().getNode().getForeignSource();
            } else if (resource.hasGeneric() && resource.getGeneric().hasNode() && !resource.getGeneric().getNode().getForeignSource().isEmpty()) {
                foreignSource = resource.getGeneric().getNode().getForeignSource();
            } else if (resource.hasResponse() && resource.getResponse().hasNode() && !resource.getResponse().getNode().getForeignSource().isEmpty()) {
                foreignSource = resource.getResponse().getNode().getForeignSource();
            }
            return foreignSource;
        }

        public String getForeignId() {
            String foreignId = null;
            if (resource.hasNode() && !resource.getNode().getForeignId().isEmpty()) {
                foreignId = resource.getNode().getForeignId();
            } else if (resource.hasInterface() && resource.getInterface().hasNode() && !resource.getInterface().getNode().getForeignId().isEmpty()) {
                foreignId = resource.getInterface().getNode().getForeignId();
            } else if (resource.hasGeneric() && resource.getGeneric().hasNode() && !resource.getGeneric().getNode().getForeignId().isEmpty()) {
                foreignId = resource.getGeneric().getNode().getForeignId();
            } else if (resource.hasResponse() && resource.getResponse().hasNode() && !resource.getResponse().getNode().getForeignId().isEmpty()) {
                foreignId = resource.getResponse().getNode().getForeignId();
            }
            return foreignId;
        }

        public String getLocation() {
            String location = null;
            if (resource.hasNode() && !resource.getNode().getLocation().isEmpty()) {
                location = resource.getNode().getLocation();
            } else if (resource.hasInterface() && resource.getInterface().hasNode() && !resource.getInterface().getNode().getLocation().isEmpty()) {
                location = resource.getInterface().getNode().getLocation();
            } else if (resource.hasGeneric() && resource.getGeneric().hasNode() && !resource.getGeneric().getNode().getLocation().isEmpty()) {
                location = resource.getGeneric().getNode().getLocation();
            } else if (resource.hasResponse() && resource.getResponse().hasNode() && !resource.getResponse().getLocation().isEmpty()) {
                location = resource.getResponse().getLocation();
            }
            return location;
        }

        public String getResourceTypeName() {
            if (resource.hasResponse()) {
                return "responseTime";
            } else if (resource.hasGeneric()) {
                return resource.getGeneric().getType();
            } else {
                return resource.getResourceTypeName();
            }
        }

        public String getResourceName() {
            if (!resource.getResourceName().isEmpty()) {
                return resource.getResourceName();
            }
            return null;
        }

        public Integer getIfIndex() {
            if (resource.hasInterface()) {
                return resource.getInterface().getIfIndex();
            }
            return null;
        }

        public String getResourceId() {
            return resource.getResourceId();
        }

        public String getInstance() {
            if (resource.hasGeneric()) {
                return resource.getGeneric().getInstance();
            } else if (resource.hasInterface()) {
                return resource.getInterface().getInstance();
            } else if (resource.hasResponse()) {
                return resource.getResponse().getInstance();
            }
            return null;
        }
    }
}
