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
import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.eventd.router.EventClassification;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.eventd.router.IpcMessageConverter;
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
 * instances) and then routes them to Kafka (fault events) or the in-process
 * {@link MessageBus} (IPC events).
 *
 * <p>Routing is determined by {@link EventClassifier}:
 * <ul>
 *   <li>{@link EventClassification#FAULT} -- published to Kafka topic only</li>
 *   <li>{@link EventClassification#IPC} -- published to MessageBus only</li>
 *   <li>{@link EventClassification#DUAL} -- published to both Kafka and MessageBus</li>
 * </ul>
 */
public class KafkaEventForwarder implements EventForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventForwarder.class);

    private final EventProcessor eventExpander;
    private final EventProcessor tsidAssigner;
    private final EventClassifier eventClassifier;
    private final IpcMessageConverter ipcMessageConverter;
    private final MessageBus messageBus;
    private final KafkaProducer<Long, byte[]> kafkaProducer;
    private final String topicName;

    public KafkaEventForwarder(EventProcessor eventExpander,
                               EventProcessor tsidAssigner,
                               EventClassifier eventClassifier,
                               IpcMessageConverter ipcMessageConverter,
                               MessageBus messageBus,
                               KafkaProducer<Long, byte[]> kafkaProducer,
                               String topicName) {
        this.eventExpander = Objects.requireNonNull(eventExpander, "eventExpander");
        this.tsidAssigner = Objects.requireNonNull(tsidAssigner, "tsidAssigner");
        this.eventClassifier = Objects.requireNonNull(eventClassifier, "eventClassifier");
        this.ipcMessageConverter = Objects.requireNonNull(ipcMessageConverter, "ipcMessageConverter");
        this.messageBus = Objects.requireNonNull(messageBus, "messageBus");
        this.kafkaProducer = Objects.requireNonNull(kafkaProducer, "kafkaProducer");
        this.topicName = Objects.requireNonNull(topicName, "topicName");
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
                publishToMessageBus(event);
                break;
            case DUAL:
                publishToKafka(event);
                publishToMessageBus(event);
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

    private void publishToMessageBus(Event event) {
        try {
            IpcMessage ipcMessage = ipcMessageConverter.convert(event);
            messageBus.publish(ipcMessage);
            LOG.debug("Published event {} to MessageBus as type {}", event.getUei(), ipcMessage.getType());
        } catch (Exception e) {
            LOG.error("Failed to publish event {} to MessageBus", event.getUei(), e);
        }
    }

    private Log wrapInLog(Event event) {
        Log log = new Log();
        log.addEvent(event);
        return log;
    }
}
