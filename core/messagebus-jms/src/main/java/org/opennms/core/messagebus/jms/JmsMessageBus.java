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
package org.opennms.core.messagebus.jms;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.messagebus.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMS/ActiveMQ-backed {@link MessageBus} implementation.
 *
 * Each message type maps to a JMS Topic with prefix "OpenNMS.IPC.". When
 * publishing, the IpcMessage fields are mapped into a JMS MapMessage. When
 * subscribing, a JMS MessageListener dispatches to the registered
 * MessageHandler.
 *
 * Thread safety: all operations are synchronized via the JMS Session (which
 * is not thread-safe per the JMS spec), or via ConcurrentHashMap for the
 * handler registry.
 */
public class JmsMessageBus implements MessageBus {

    private static final Logger LOG = LoggerFactory.getLogger(JmsMessageBus.class);
    private static final String TOPIC_PREFIX = "OpenNMS.IPC.";

    private final ConnectionFactory connectionFactory;
    private Connection connection;
    private Session publishSession;
    private Session subscribeSession;
    private final Map<String, MessageProducer> producers = new ConcurrentHashMap<>();
    private final Map<MessageHandler, MessageConsumer> consumers = new ConcurrentHashMap<>();
    private volatile boolean started;

    public JmsMessageBus(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public synchronized void start() throws JMSException {
        if (started) {
            return;
        }
        connection = connectionFactory.createConnection();
        publishSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        subscribeSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        started = true;
        LOG.info("JmsMessageBus started");
    }

    public synchronized void stop() {
        started = false;
        for (MessageConsumer consumer : consumers.values()) {
            closeQuietly(consumer);
        }
        consumers.clear();
        for (MessageProducer producer : producers.values()) {
            closeQuietly(producer);
        }
        producers.clear();
        closeQuietly(publishSession);
        closeQuietly(subscribeSession);
        closeQuietly(connection);
        LOG.info("JmsMessageBus stopped");
    }

    @Override
    public void publish(IpcMessage message) {
        ensureStarted();
        try {
            MessageProducer producer = getOrCreateProducer(message.getType());
            MapMessage jmsMessage = toJmsMessage(message);
            producer.send(jmsMessage);
            LOG.debug("Published IPC message type={} source={}", message.getType(), message.getSource());
        } catch (JMSException e) {
            LOG.error("Failed to publish IPC message type={}", message.getType(), e);
        }
    }

    @Override
    public void subscribe(String messageType, MessageHandler handler) {
        ensureStarted();
        try {
            String topicName = TOPIC_PREFIX + messageType;
            Topic topic = subscribeSession.createTopic(topicName);
            MessageConsumer consumer = subscribeSession.createConsumer(topic);
            consumer.setMessageListener(new HandlerAdapter(handler));
            consumers.put(handler, consumer);
            LOG.info("Subscribed handler {} to topic {}", handler.getName(), topicName);
        } catch (JMSException e) {
            LOG.error("Failed to subscribe handler {} to type {}", handler.getName(), messageType, e);
        }
    }

    @Override
    public void subscribe(Collection<String> messageTypes, MessageHandler handler) {
        for (String type : messageTypes) {
            subscribe(type, handler);
        }
    }

    @Override
    public void unsubscribe(MessageHandler handler) {
        MessageConsumer consumer = consumers.remove(handler);
        if (consumer != null) {
            closeQuietly(consumer);
            LOG.info("Unsubscribed handler {}", handler.getName());
        }
    }

    private MessageProducer getOrCreateProducer(String messageType) throws JMSException {
        MessageProducer producer = producers.get(messageType);
        if (producer != null) {
            return producer;
        }
        synchronized (this) {
            producer = producers.get(messageType);
            if (producer != null) {
                return producer;
            }
            String topicName = TOPIC_PREFIX + messageType;
            Topic topic = publishSession.createTopic(topicName);
            producer = publishSession.createProducer(topic);
            producers.put(messageType, producer);
            return producer;
        }
    }

    private MapMessage toJmsMessage(IpcMessage message) throws JMSException {
        MapMessage jmsMessage = publishSession.createMapMessage();
        jmsMessage.setString("type", message.getType());
        jmsMessage.setString("source", message.getSource());
        jmsMessage.setLong("timestamp", message.getTimestamp());
        if (message.getNodeId() != null) {
            jmsMessage.setLong("nodeId", message.getNodeId());
        }
        if (message.getInterfaceAddress() != null) {
            jmsMessage.setString("interfaceAddress", message.getInterfaceAddress());
        }
        for (Map.Entry<String, String> entry : message.getParameters().entrySet()) {
            jmsMessage.setString("param." + entry.getKey(), entry.getValue());
        }
        return jmsMessage;
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("JmsMessageBus has not been started");
        }
    }

    private static void closeQuietly(Connection c) {
        if (c != null) { try { c.close(); } catch (JMSException e) { LOG.debug("Error closing Connection", e); } }
    }

    private static void closeQuietly(Session s) {
        if (s != null) { try { s.close(); } catch (JMSException e) { LOG.debug("Error closing Session", e); } }
    }

    private static void closeQuietly(MessageProducer p) {
        if (p != null) { try { p.close(); } catch (JMSException e) { LOG.debug("Error closing MessageProducer", e); } }
    }

    private static void closeQuietly(MessageConsumer c) {
        if (c != null) { try { c.close(); } catch (JMSException e) { LOG.debug("Error closing MessageConsumer", e); } }
    }

    /**
     * Adapts a JMS MessageListener to a MessageHandler by converting
     * JMS MapMessages back into IpcMessage instances.
     */
    private static class HandlerAdapter implements MessageListener {
        private final MessageHandler handler;

        HandlerAdapter(MessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onMessage(Message message) {
            try {
                if (message instanceof MapMessage) {
                    IpcMessage ipcMessage = fromJmsMessage((MapMessage) message);
                    handler.onMessage(ipcMessage);
                } else {
                    LOG.warn("Received non-MapMessage: {}", message.getClass().getName());
                }
            } catch (Exception e) {
                LOG.warn("Handler {} failed processing JMS message", handler.getName(), e);
            }
        }

        private IpcMessage fromJmsMessage(MapMessage msg) throws JMSException {
            String type = msg.getString("type");
            String source = msg.getString("source");
            long timestamp = msg.getLong("timestamp");
            Long nodeId = msg.itemExists("nodeId") ? msg.getLong("nodeId") : null;
            String interfaceAddress = msg.itemExists("interfaceAddress")
                    ? msg.getString("interfaceAddress") : null;

            java.util.HashMap<String, String> parameters = new java.util.HashMap<>();
            @SuppressWarnings("unchecked")
            java.util.Enumeration<String> names = msg.getMapNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (name.startsWith("param.")) {
                    parameters.put(name.substring("param.".length()), msg.getString(name));
                }
            }

            return new IpcMessage(type, source, timestamp, nodeId, interfaceAddress, parameters);
        }
    }
}
