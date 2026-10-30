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

import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.features.kafka.producer.KafkaProducerManager.MessageType;
import org.opennms.features.kafka.producer.ProducerMetrics;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KafkaPersister implements Persister {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersister.class);

    private static final int MAX_BUFFER_SIZE_CONFIGURED = 921600;

    // NumericAttribute.type must always print: GAUGE is the zero enum value, which the
    // printer would otherwise omit, leaving gauges without a type key while counters keep theirs
    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
            .omittingInsignificantWhitespace()
            .includingDefaultValueFields(Collections.singleton(
                    CollectionSetProtos.NumericAttribute.getDescriptor()
                            .findFieldByNumber(CollectionSetProtos.NumericAttribute.TYPE_FIELD_NUMBER)));

    private CollectionSetMapper collectionSetMapper;

    private final ServiceParameters m_params;

    private Producer<String, byte[]> producer;

    private String topicName = "metrics";

    private Boolean disableMetricsSplitting = false;

    private boolean useJson = false;

    private MetricFilter metricFilter;

    private MetricTopicRouter metricTopicRouter;

    private ProducerMetrics metrics;

    public KafkaPersister(ServiceParameters params) {
        m_params = params;
    }

    public KafkaPersister() {
        m_params = new ServiceParameters(Collections.emptyMap());
    }

    /** {@inheritDoc} */
    @Override
    public void visitCollectionSet(CollectionSet collectionSet) {
        // With metric routing disabled the router resolves every resource to topicName, so this
        // yields a single group and behaves exactly as the un-routed path did.
        final Map<String, CollectionSetProtos.CollectionSet> collectionSetProtosByTopic;
        try (Timer.Context ignored = metrics != null ? metrics.build(MessageType.METRIC).time() : null) {
            collectionSetProtosByTopic = collectionSetMapper
                    .buildCollectionSetProtosByTopic(collectionSet, m_params, metricTopicRouter, topicName);
        }
        if (collectionSetProtosByTopic == null) {
            return;
        }
        for (final Map.Entry<String, CollectionSetProtos.CollectionSet> entry : collectionSetProtosByTopic.entrySet()) {
            final CollectionSetProtos.CollectionSet collectionSetProto = entry.getValue();
            if (collectionSetProto == null) {
                continue;
            }
            // Apply filtering if configured. The filter is evaluated per resource, so filtering
            // each group is equivalent to filtering the CollectionSet as a whole.
            final CollectionSetProtos.CollectionSet filteredCollectionSetProto = metricFilter != null
                    ? metricFilter.apply(collectionSetProto) : collectionSetProto;
            if (filteredCollectionSetProto != null && filteredCollectionSetProto.getResourceCount() > 0) {
                countRoutedResources(entry.getKey(), filteredCollectionSetProto.getResourceCount());
                bisectAndSendMessageToKafka(filteredCollectionSetProto, entry.getKey());
            }
        }
    }

    private void countRoutedResources(final String topic, final int resourceCount) {
        if (metricTopicRouter == null || !metricTopicRouter.isEnabled()) {
            return;
        }
        metricTopicRouter.getMetricRegistry()
                .counter(MetricTopicRouter.METRIC_ROUTED_PREFIX + "." + topic)
                .inc(resourceCount);
    }

    /**
     * Retained for callers - and tests - that send to the persister's configured topic.
     */
    void bisectAndSendMessageToKafka(CollectionSetProtos.CollectionSet collectionSetProto) {
        bisectAndSendMessageToKafka(collectionSetProto, topicName);
    }

    void bisectAndSendMessageToKafka(CollectionSetProtos.CollectionSet collectionSetProto, String topic) {
        final byte[] payload = serializeCollectionSet(collectionSetProto);
        if (payload == null) {
            return;
        }
        if (!getDisableMetricsSplitting() && checkForMaxSize(payload.length)) {
            if(collectionSetProto.getResourceCount() == 1) {
                /// Handle the case where resource is only one with too many attributes that can cross max buffer size.
                CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
                if(collectionSetResource.getNumericList().size() > 0) {
                    // Handle numeric attributes only.
                    CollectionSetProtos.CollectionSetResource.Builder numericResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                    numericResourceBuilder.mergeFrom(collectionSetResource).clearString();
                    CollectionSetProtos.CollectionSet collectionSetWithNumeric = CollectionSetProtos.CollectionSet.newBuilder()
                            .addResource(numericResourceBuilder).setTimestamp(collectionSetProto.getTimestamp()).build();
                    bisectNumericAttributes(collectionSetWithNumeric, topic);
                }
                if(collectionSetResource.getStringList().size() > 0) {
                    // Handle string attributes only
                    CollectionSetProtos.CollectionSetResource.Builder stringResourceBuilder = CollectionSetProtos.CollectionSetResource.newBuilder();
                    stringResourceBuilder.mergeFrom(collectionSetResource).clearNumeric();
                    CollectionSetProtos.CollectionSet collectionSetWithStringAttributes = CollectionSetProtos.CollectionSet.newBuilder()
                            .addResource(stringResourceBuilder).setTimestamp(collectionSetProto.getTimestamp()).build();
                    bisectStringAttributes(collectionSetWithStringAttributes, topic);
                }
            } else {
                // Divide resources into two in recursive way.
                Iterator<List<CollectionSetResource>> subList = Iterables.partition(collectionSetProto.getResourceList(),
                        (collectionSetProto.getResourceCount() + 1) / 2).iterator();

                CollectionSetProtos.CollectionSet firstPartCollectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                        .mergeFrom(collectionSetProto).clearResource().addAllResource(subList.next()).build();
                bisectAndSendMessageToKafka(firstPartCollectionSet, topic);

                CollectionSetProtos.CollectionSet secondPartCollectionSet = CollectionSetProtos.CollectionSet.newBuilder()
                        .mergeFrom(collectionSetProto).clearResource().addAllResource(subList.next()).build();
                bisectAndSendMessageToKafka(secondPartCollectionSet, topic);
            }
        } else {
            sendMessageToKafka(collectionSetProto, payload, topic);
        }
    }

    private void bisectNumericAttributes(CollectionSetProtos.CollectionSet collectionSetProto, String topic) {
        // Divide numeric attributes into two in recursive way
        final byte[] payload = serializeCollectionSet(collectionSetProto);
        if (payload == null) {
            return;
        }
        final boolean oversized = checkForMaxSize(payload.length);
        if (oversized && collectionSetProto.getResource(0).getNumericCount() > 1) {
            Iterator<List<CollectionSetProtos.NumericAttribute>> subList = Iterables.partition(collectionSetProto.getResource(0).getNumericList(),
                    (collectionSetProto.getResource(0).getNumericCount() + 1) / 2).iterator();
            bisectNumericAttributes(buildCollectionSetWithNumericAttributes(collectionSetProto, subList.next()), topic);
            bisectNumericAttributes(buildCollectionSetWithNumericAttributes(collectionSetProto, subList.next()), topic);
        } else {
            if (oversized) {
                LOG.warn("A single attribute encodes to {} bytes, exceeding the maximum of {} bytes; it cannot be split further and will be sent as-is",
                        payload.length, MAX_BUFFER_SIZE_CONFIGURED);
            }
            sendMessageToKafka(collectionSetProto, payload, topic);
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

    private void bisectStringAttributes(CollectionSetProtos.CollectionSet collectionSetProto, String topic) {
        // Divide string attributes into two in recursive way
        final byte[] payload = serializeCollectionSet(collectionSetProto);
        if (payload == null) {
            return;
        }
        final boolean oversized = checkForMaxSize(payload.length);
        if (oversized && collectionSetProto.getResource(0).getStringCount() > 1) {
            Iterator<List<CollectionSetProtos.StringAttribute>> subList = Iterables.partition(collectionSetProto.getResource(0).getStringList(),
                    (collectionSetProto.getResource(0).getStringCount() + 1) / 2).iterator();
            bisectStringAttributes(buildCollectionSetWithStringAttributes(collectionSetProto, subList.next()), topic);
            bisectStringAttributes(buildCollectionSetWithStringAttributes(collectionSetProto, subList.next()), topic);
        } else {
            if (oversized) {
                LOG.warn("A single attribute encodes to {} bytes, exceeding the maximum of {} bytes; it cannot be split further and will be sent as-is",
                        payload.length, MAX_BUFFER_SIZE_CONFIGURED);
            }
            sendMessageToKafka(collectionSetProto, payload, topic);
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

    /**
     * Renders the collection set exactly as it would be forwarded when {@code metrics.useJson} is enabled.
     */
    public static String toJson(CollectionSetProtos.CollectionSet collectionSetProto) throws InvalidProtocolBufferException {
        return JSON_PRINTER.print(collectionSetProto);
    }

    /**
     * Serialize the CollectionSet with the configured output format:
     * JSON when useJson is enabled, protobuf otherwise.
     * Returns null if the CollectionSet could not be serialized.
     */
    byte[] serializeCollectionSet(CollectionSetProtos.CollectionSet collectionSetProto) {
        if (useJson) {
            try {
                return toJson(collectionSetProto).getBytes(StandardCharsets.UTF_8);
            } catch (InvalidProtocolBufferException e) {
                LOG.error("Failed to serialize collection set with {} resources (key {}) to JSON, it will not be forwarded",
                        collectionSetProto.getResourceCount(), deriveKeyFromCollectionSet(collectionSetProto), e);
                return null;
            }
        }
        return collectionSetProto.toByteArray();
    }

    private void sendMessageToKafka(CollectionSetProtos.CollectionSet collectionSetProto, byte[] payload, String topic) {
        // If no resources should be persisted, do not send an empty CollectionSet
        if (collectionSetProto.getResourceCount() == 0) {
            return;
        }
        // Derive key, it will be nodeId for all resources except for response time, it would be IpAddress
        final String key = deriveKeyFromCollectionSet(collectionSetProto);
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, payload);
        if (metrics != null) {
            metrics.size(MessageType.METRIC, payload);
        }
        producer.send(record, (recordMetadata, e) -> {
            if (e != null) {
                if (metrics != null) {
                    metrics.failed(MessageType.METRIC);
                }
                LOG.warn("Failed to send record to producer: {}.", record, e);
                return;
            }
            if (metrics != null) {
                metrics.sent(MessageType.METRIC);
            }
            if (LOG.isDebugEnabled()) {
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

    public void setMetricTopicRouter(MetricTopicRouter metricTopicRouter) {
        this.metricTopicRouter = metricTopicRouter;
        this.metrics = metricTopicRouter != null ? new ProducerMetrics(metricTopicRouter.getMetricRegistry()) : null;
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

    public void setUseJson(boolean useJson) {
        this.useJson = useJson;
    }

    public void setMetricFilter(final String metricFilter) {
        this.metricFilter = MetricFilter.parse(metricFilter);
    }

    public MetricFilter getMetricFilter() {
        return metricFilter;
    }

    /**
     * @deprecated the SpEL root object moved to {@link MetricFilter.ResourceFilterContext}; this alias only
     * keeps external code compiling.
     */
    @Deprecated
    public static class ResourceFilterContext extends MetricFilter.ResourceFilterContext {
        public ResourceFilterContext(CollectionSetProtos.CollectionSetResource resource) {
            super(resource);
        }
    }
}
