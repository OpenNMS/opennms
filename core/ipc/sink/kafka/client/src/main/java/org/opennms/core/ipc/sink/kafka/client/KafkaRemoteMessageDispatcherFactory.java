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
package org.opennms.core.ipc.sink.kafka.client;

import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.DEFAULT_MAX_BUFFER_SIZE;
import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.KAFKA_COMMON_CONFIG_PID;
import static org.opennms.core.ipc.common.kafka.KafkaSinkConstants.MAX_BUFFER_SIZE_PROPERTY;
import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_PRODUCER_DOMAIN;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.KafkaSinkConstants;
import org.opennms.core.ipc.common.kafka.OsgiKafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.ipc.sink.model.SinkMessage;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.distributed.core.api.Identity;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.math.IntMath;
import com.google.protobuf.ByteString;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;

public class KafkaRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<String> {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaRemoteMessageDispatcherFactory.class);

    private final static int INVALID_PARTITION = -1;
    private final Properties kafkaConfig = new Properties();

    private ConfigurationAdmin configAdmin;

    private BundleContext bundleContext;

    private KafkaProducer<String, byte[]> producer;

    private TracerRegistry tracerRegistry;

    private MetricRegistry metrics;

    private Identity identity;

    private int maxBufferSize;

    @Override
    public <S extends Message, T extends Message> String getModuleMetadata(final SinkModule<S, T> module) {
        final JmsQueueNameFactory topicNameFactory = new JmsQueueNameFactory(KafkaSinkConstants.KAFKA_TOPIC_PREFIX, module.getId());
        return topicNameFactory.getName();
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String topic, T message) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.trace("dispatch({}): sending message {}", topic, message);
            byte[] sinkMessageContent = module.marshal(message);
            String messageId = UUID.randomUUID().toString();
            final String messageKey = module.getRoutingKey(message).orElse(messageId);
            sendMessage(topic, messageId, messageKey, sinkMessageContent);
        }
    }

    /**
     * Divides the message into chunks and sends them to Kafka. The first chunk is partitioned by key; every
     * following chunk is sent to that same partition explicitly, so one message never straddles partitions
     * even if the partition count changes while it is being sent.
     * @param topic    The kafka topic message needs to be sent
     * @param messageId  The messageId message associated with
     * @param messageKey  The key used to route the message
     * @param sinkMessageContent  The sink message
     */
    private void sendMessage(String topic, String messageId, String messageKey, byte[] sinkMessageContent) {
        int partition = INVALID_PARTITION;
        int totalChunks = IntMath.divide(sinkMessageContent.length, maxBufferSize, RoundingMode.UP);
        for (int chunk = 0; chunk < totalChunks; chunk++) {
            byte[] messageInBytes = wrapMessageToProto(messageId, chunk, totalChunks, sinkMessageContent);
            final ProducerRecord<String, byte[]> record = (chunk == 0)
                    ? new ProducerRecord<>(topic, messageKey, messageInBytes)
                    : new ProducerRecord<>(topic, partition, messageKey, messageInBytes);
            // Add tags to tracer active span.
            Span activeSpan = getTracer().activeSpan();
            if (activeSpan != null && (chunk + 1 == totalChunks)) {
                activeSpan.setTag(TracerConstants.TAG_TOPIC, topic);
                activeSpan.setTag(TracerConstants.TAG_MESSAGE_SIZE, sinkMessageContent.length);
                activeSpan.setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            }
            // Keep sending record till it delivers successfully.
            int sentTo = sendMessageChunkToKafka(topic, record);
            if (sentTo == INVALID_PARTITION) {
                if (totalChunks > 1) {
                    LOG.error("Failed to send chunk {} of {} for message {} to topic {}, dropping the message.",
                            chunk, totalChunks, messageId, topic);
                }
                return;
            }
            partition = sentTo;
        }
    }

    /**
     *  This method will send one chunk of message to kafka and returns the partition number the message has been sent to.
     * @param topic   The kafka topic message needs to be sent
     * @param record message
     * @return  partition number
     */
    private int sendMessageChunkToKafka(String topic, ProducerRecord<String, byte[]> record) {

        while (true) {
            try {
                // From KafkaProducer's JavaDoc: The producer is thread safe and should generally be shared among all threads for best performance.
                final Future<RecordMetadata> future = producer.send(record);
                // The call to dispatch() is synchronous, so we block until the message was sent
                RecordMetadata recordMetadata = future.get();
                return recordMetadata.partition();
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while sending message to topic {}.", topic, e);
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                // Timeout typically happens when Kafka is Offline or it didn't initialize yet.
                // For this case keep sending the message until it delivers, will cause sink messages to buffer.
                if (cause instanceof TimeoutException) {
                    LOG.warn("Timeout occurred while sending message to topic '{}', it will be attempted again.", topic);
                } else if (cause instanceof UnknownTopicOrPartitionException) {
                    // Topic might not exist yet if auto.create.topics.enable is true but delayed,
                    // or topic is being created. Keep retrying as this can be transient.
                    LOG.warn("Topic '{}' does not exist yet, it will be attempted again. " +
                            "If auto.create.topics.enable=false, ensure this topic is created manually.", topic);
                } else {
                    LOG.error("Exception occurred while sending message to topic '{}': {}", topic, e.getMessage(), e);
                    break;
                }
            }
        }
      return INVALID_PARTITION;
    }

    private byte[] wrapMessageToProto(String messageId, int chunk, int totalChunks, byte[] sinkMessageContent) {
        // Calculate remaining bufferSize for each chunk.
        int bufferSize = getRemainingBufferSize(sinkMessageContent.length, chunk);
        ByteString byteString = ByteString.copyFrom(sinkMessageContent, chunk * maxBufferSize, bufferSize);
        SinkMessage.Builder sinkMessageBuilder = SinkMessage.newBuilder()
                .setMessageId(messageId)
                .setCurrentChunkNumber(chunk)
                .setTotalChunks(totalChunks)
                .setContent(byteString);
        // Add tracing info
        final Tracer tracer = getTracer();
        if (tracer.activeSpan() != null && (chunk + 1 == totalChunks)) {
            TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
            tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
            tracer.activeSpan().setTag(TracerConstants.TAG_LOCATION, identity.getLocation());
            tracer.activeSpan().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            tracingInfoCarrier.getTracingInfoMap().forEach(sinkMessageBuilder::putTracingInfo);
        }
        return sinkMessageBuilder.build().toByteArray();
    }

    public void init() throws IOException {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            // Defaults
            kafkaConfig.clear();
            kafkaConfig.put("key.serializer", StringSerializer.class.getCanonicalName());
            kafkaConfig.put("value.serializer", ByteArraySerializer.class.getCanonicalName());
            // Retrieve all of the properties from org.opennms.core.ipc.sink.kafka.cfg, fallback to common pid.
            KafkaConfigProvider configProvider = new OsgiKafkaConfigProvider(KafkaSinkConstants.KAFKA_CONFIG_PID, configAdmin, KAFKA_COMMON_CONFIG_PID);
            kafkaConfig.putAll(configProvider.getProperties());
            LOG.info("KafkaRemoteMessageDispatcherFactory: initializing the Kafka producer with: {}", kafkaConfig);
            producer = Utils.runWithGivenClassLoader(() -> new KafkaProducer<>(kafkaConfig), KafkaProducer.class.getClassLoader());
            maxBufferSize = getMaxBufferSize();
            if (tracerRegistry != null && identity != null) {
                tracerRegistry.init(identity.getLocation() + "@" + identity.getId());
            }
            onInit();
        }
    }

    public void destroy() {
        onDestroy();
        if (producer != null) {
            producer.close();
            producer = null;
        }
    }

    @Override
    public String getMetricDomain() {
        return SINK_METRIC_PRODUCER_DOMAIN;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    // Calculate remaining buffer size for each chunk.
    private int getRemainingBufferSize(int messageSize, int chunk) {
        int bufferSize = messageSize;
        if (messageSize > maxBufferSize) {
            int remaining = messageSize - chunk * maxBufferSize;
            bufferSize = (remaining > maxBufferSize) ? maxBufferSize : remaining;
        }
        return bufferSize;
    }

    public Integer getMaxBufferSize() {
        int maxBufferSize = DEFAULT_MAX_BUFFER_SIZE;
        Object bufferSize = kafkaConfig.get(MAX_BUFFER_SIZE_PROPERTY);
        if (bufferSize != null) {
            try {
                int configured = Integer.parseInt(bufferSize.toString().trim());
                if (configured > 0) {
                    maxBufferSize = configured;
                } else {
                    LOG.warn("Configured max buffer size {} is not positive, using default {}", configured, DEFAULT_MAX_BUFFER_SIZE);
                }
            } catch (NumberFormatException ex){
                LOG.warn("Configured max buffer size '{}' is not a number, using default {}", bufferSize, DEFAULT_MAX_BUFFER_SIZE);
            }
        }
        return Math.min(DEFAULT_MAX_BUFFER_SIZE, maxBufferSize);
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    @Override
    public Tracer getTracer() {
        if (getTracerRegistry() != null) {
            return getTracerRegistry().getTracer();
        }
        return  GlobalTracer.get();
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @Override
    public MetricRegistry getMetrics() {
        if(metrics == null) {
            metrics = new MetricRegistry();
        }
        return metrics;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }
}
