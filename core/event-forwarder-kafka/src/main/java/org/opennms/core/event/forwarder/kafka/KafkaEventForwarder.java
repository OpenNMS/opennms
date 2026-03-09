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
package org.opennms.core.event.forwarder.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.eventd.router.EventClassification;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EventForwarder} implementation that enriches events locally
 * (via an event expander and a TSID assigner, both {@link EventProcessor}
 * instances) and then routes them to Kafka topics based on classification.
 *
 * <p>Routing is determined by {@link EventClassifier}:
 * <ul>
 *   <li>{@link EventClassification#FAULT} -- published to fault Kafka topic</li>
 *   <li>{@link EventClassification#IPC} -- published to IPC Kafka topic</li>
 *   <li>{@link EventClassification#DUAL} -- published to both Kafka topics</li>
 * </ul>
 */
public class KafkaEventForwarder implements EventForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventForwarder.class);

    private final EventProcessor eventExpander;
    private final EventProcessor tsidAssigner;
    private final EventClassifier eventClassifier;
    private final KafkaProducer<Long, byte[]> kafkaProducer;
    private final String topicName;
    private volatile String ipcTopicName; // injected via setter; null until configured

    /**
     * @param eventExpander expands events using eventconf; use {@link NoOpEventProcessor}
     *                      in daemon containers where eventconf is unavailable
     */
    public KafkaEventForwarder(EventProcessor eventExpander,
                               EventProcessor tsidAssigner,
                               EventClassifier eventClassifier,
                               KafkaProducer<Long, byte[]> kafkaProducer,
                               String topicName) {
        this.eventExpander = Objects.requireNonNull(eventExpander, "eventExpander");
        this.tsidAssigner = Objects.requireNonNull(tsidAssigner, "tsidAssigner");
        this.eventClassifier = Objects.requireNonNull(eventClassifier, "eventClassifier");
        this.kafkaProducer = Objects.requireNonNull(kafkaProducer, "kafkaProducer");
        this.topicName = Objects.requireNonNull(topicName, "topicName");
    }

    /**
     * Factory method for Blueprint. Aries Blueprint 1.10.3 can't match constructor
     * args when multiple params share the same interface type (EventProcessor) and
     * a generic type (KafkaProducer). Factory methods bypass constructor matching.
     */
    public static KafkaEventForwarder create(EventProcessor eventExpander,
                                              EventProcessor tsidAssigner,
                                              EventClassifier eventClassifier,
                                              KafkaProducer<Long, byte[]> kafkaProducer,
                                              String topicName) {
        return new KafkaEventForwarder(eventExpander, tsidAssigner, eventClassifier,
                kafkaProducer, topicName);
    }

    public void setIpcTopicName(String ipcTopicName) {
        this.ipcTopicName = ipcTopicName;
    }

    @Override
    public void sendNow(Event event) {
        Log log = wrapInLog(event);
        enrichAndRoute(log);
    }

    @Override
    public void sendNow(Log eventLog) {
        enrichAndRoute(eventLog);
    }

    @Override
    public void sendNowSync(Event event) {
        // Synchronous path uses the same enrichment and routing pipeline
        sendNow(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        // Synchronous path uses the same enrichment and routing pipeline
        sendNow(eventLog);
    }

    private void enrichAndRoute(Log log) {
        try {
            eventExpander.process(log);
        } catch (EventProcessorException e) {
            LOG.error("EventExpander failed, events will not be routed", e);
            return;
        }

        try {
            tsidAssigner.process(log);
        } catch (EventProcessorException e) {
            LOG.error("TsidAssigner failed, events will not be routed", e);
            return;
        }

        if (log.getEvents() == null || log.getEvents().getEventCollection() == null) {
            return;
        }

        for (Event event : log.getEvents().getEventCollection()) {
            routeEvent(event);
        }
    }

    private void routeEvent(Event event) {
        EventClassification classification = eventClassifier.classify(event);
        LOG.debug("Routing event {} as {}", event.getUei(), classification);

        switch (classification) {
            case FAULT:
                publishToKafka(event);
                break;
            case IPC:
                publishToIpcKafka(event);
                break;
            case DUAL:
                publishToKafka(event);
                publishToIpcKafka(event);
                break;
            default:
                LOG.warn("Unknown classification {} for event {}", classification, event.getUei());
                break;
        }
    }

    private void publishToKafka(Event event) {
        try {
            byte[] payload = JaxbUtils.marshal(event).getBytes(StandardCharsets.UTF_8);
            long key = event.getNodeid(); // returns 0L when nodeId is null
            ProducerRecord<Long, byte[]> record = new ProducerRecord<>(topicName, key, payload);
            kafkaProducer.send(record);
            LOG.debug("Published event {} to Kafka topic {} with key {}", event.getUei(), topicName, key);
        } catch (Exception e) {
            LOG.error("Failed to publish event {} to Kafka", event.getUei(), e);
        }
    }

    private void publishToIpcKafka(Event event) {
        if (ipcTopicName == null) {
            LOG.debug("IPC topic not configured, dropping IPC event {}", event.getUei());
            return;
        }
        try {
            byte[] payload = JaxbUtils.marshal(event).getBytes(StandardCharsets.UTF_8);
            long key = event.getNodeid(); // returns 0L when nodeId is null
            ProducerRecord<Long, byte[]> record = new ProducerRecord<>(ipcTopicName, key, payload);
            kafkaProducer.send(record);
            LOG.debug("Published IPC event {} to Kafka topic {} with key {}", event.getUei(), ipcTopicName, key);
        } catch (Exception e) {
            LOG.error("Failed to publish IPC event {} to Kafka", event.getUei(), e);
        }
    }

    private Log wrapInLog(Event event) {
        Log log = new Log();
        log.addEvent(event);
        return log;
    }
}
