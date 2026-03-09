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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.eventd.router.EventClassification;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.ManagedObject;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.UpdateField;
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

    private static final Pattern PARM_PATTERN = Pattern.compile("%([^%]+)%");

    private final EventProcessor eventExpander;
    private final EventProcessor tsidAssigner;
    private final EventClassifier eventClassifier;
    private final KafkaProducer<Long, byte[]> kafkaProducer;
    private final String topicName;
    private volatile String ipcTopicName; // injected via setter; null until configured
    private volatile EventConfDao eventConfDao; // optional; enriches events with eventconf data

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

    public void setEventConfDao(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
        LOG.info("EventConfDao injected — events will be enriched with eventconf data (severity, alarm-data)");
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

        // Apply eventconf enrichment (severity, alarm-data) for events that
        // don't already have it. In daemon containers the eventExpander is a
        // NoOpEventProcessor, so this fills the gap that EventExpander/Eventd
        // would fill in the monolith.
        enrichWithEventConf(log);

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

    // -------- Eventconf enrichment --------

    private void enrichWithEventConf(Log log) {
        EventConfDao dao = this.eventConfDao;
        if (dao == null) {
            return;
        }
        if (log.getEvents() == null || log.getEvents().getEventCollection() == null) {
            return;
        }
        for (Event event : log.getEvents().getEventCollection()) {
            if (event.getAlarmData() != null) {
                continue; // already enriched (e.g., by Trapd's EventCreator)
            }
            try {
                org.opennms.netmgt.xml.eventconf.Event econf = dao.findByEvent(event);
                if (econf == null) {
                    continue;
                }
                if (event.getSeverity() == null && econf.getSeverity() != null) {
                    event.setSeverity(econf.getSeverity());
                }
                if (econf.getAlarmData() != null) {
                    applyAlarmData(event, econf.getAlarmData());
                    LOG.debug("Enriched event {} with alarm-data from eventconf", event.getUei());
                }
            } catch (Exception e) {
                LOG.warn("Failed to enrich event {} with eventconf: {}", event.getUei(), e.getMessage());
            }
        }
    }

    private static void applyAlarmData(Event event, org.opennms.netmgt.xml.eventconf.AlarmData econfAlarmData) {
        AlarmData alarmData = new AlarmData();
        alarmData.setAlarmType(econfAlarmData.getAlarmType());
        alarmData.setReductionKey(expandParms(econfAlarmData.getReductionKey(), event));
        alarmData.setAutoClean(econfAlarmData.getAutoClean());
        alarmData.setX733AlarmType(econfAlarmData.getX733AlarmType());
        alarmData.setX733ProbableCause(econfAlarmData.getX733ProbableCause());
        alarmData.setClearKey(expandParms(econfAlarmData.getClearKey(), event));

        List<org.opennms.netmgt.xml.eventconf.UpdateField> updateFieldList = econfAlarmData.getUpdateFields();
        if (!updateFieldList.isEmpty()) {
            List<UpdateField> updateFields = new ArrayList<>(updateFieldList.size());
            for (org.opennms.netmgt.xml.eventconf.UpdateField econfUpdateField : updateFieldList) {
                UpdateField eventField = new UpdateField();
                eventField.setFieldName(econfUpdateField.getFieldName());
                eventField.setUpdateOnReduction(econfUpdateField.getUpdateOnReduction());
                updateFields.add(eventField);
            }
            alarmData.setUpdateField(updateFields);
        }

        org.opennms.netmgt.xml.eventconf.ManagedObject econfMo = econfAlarmData.getManagedObject();
        if (econfMo != null) {
            ManagedObject mo = new ManagedObject();
            mo.setType(econfMo.getType());
            alarmData.setManagedObject(mo);
        }

        event.setAlarmData(alarmData);
    }

    static String expandParms(String template, Event event) {
        if (template == null) {
            return null;
        }
        Matcher m = PARM_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String token = m.group(1);
            String replacement = resolveToken(token, event);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement != null ? replacement : ""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String resolveToken(String token, Event event) {
        switch (token) {
            case "uei":
                return event.getUei();
            case "nodeid":
                return event.getNodeid() != null ? String.valueOf(event.getNodeid()) : "";
            case "interface":
                return event.getInterface() != null ? event.getInterface() : "";
            case "dpname":
                return event.getDistPoller() != null ? event.getDistPoller() : "";
            case "severity":
                return event.getSeverity() != null ? event.getSeverity() : "";
            case "source":
                return event.getSource() != null ? event.getSource() : "";
            default:
                if (token.startsWith("parm[") && token.endsWith("]")) {
                    String parmRef = token.substring(5, token.length() - 1);
                    return resolveParm(parmRef, event);
                }
                return "";
        }
    }

    private static String resolveParm(String parmRef, Event event) {
        List<Parm> parms = event.getParmCollection();
        if (parms == null || parms.isEmpty()) {
            return "";
        }
        if (parmRef.startsWith("#")) {
            try {
                int index = Integer.parseInt(parmRef.substring(1)) - 1;
                if (index >= 0 && index < parms.size()) {
                    return parms.get(index).getValue().getContent();
                }
            } catch (NumberFormatException e) {
                // fall through
            }
            return "";
        }
        for (Parm parm : parms) {
            if (parmRef.equals(parm.getParmName())) {
                return parm.getValue() != null ? parm.getValue().getContent() : "";
            }
        }
        return "";
    }
}
