/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aws.sqs;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.codahale.metrics.JmxReporter;

/**
 * A factory for creating AwsRemoteMessageDispatcher objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AwsRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<String> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AwsRemoteMessageDispatcherFactory.class);

    /** The AWS configuration. */
    private final Properties awsConfig = new Properties();

    /** The configuration administration object. */
    private ConfigurationAdmin configAdmin;

    /** The reporter. */
    private JmxReporter reporter;

    /** The SQS session. */
    private Session session;

    /** The SQS Connection. */
    private SQSConnection connection;

    /** The producers. */
    private Map<String,MessageProducer> producers = new HashMap<>();

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#getModuleMetadata(org.opennms.core.ipc.sink.api.SinkModule)
     */
    @Override
    public <S extends Message, T extends Message> String getModuleMetadata(final SinkModule<S, T> module) {
        return AwsUtils.getQueueName(module);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#dispatch(org.opennms.core.ipc.sink.api.SinkModule, java.lang.Object, org.opennms.core.ipc.sink.api.Message)
     */
    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String topic, T message) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.trace("dispatch({}): sending message {}", topic, message);

            try {
                TextMessage msg = session.createTextMessage(module.marshal((T)message));
                getProducer(getModuleMetadata(module)).send(msg);
            } catch (JMSException e) {
                LOG.error("Error occured while sending message to topic {}.", topic, e);
            }
        }
    }

    /**
     * Initializes the producers.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void init() throws IOException {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            registerJmxReporter();

            // Defaults
            awsConfig.clear();

            // Retrieve all of the properties from org.opennms.core.ipc.sink.aws.cfg
            final Dictionary<String, Object> properties = configAdmin.getConfiguration(AwsSinkConstants.AWS_CONFIG_PID).getProperties();
            if (properties != null) {
                final Enumeration<String> keys = properties.keys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    awsConfig.put(key, properties.get(key));
                }
            }

            LOG.info("AwsRemoteMessageDispatcherFactory: initializing the AWS SQS producer with: {}", awsConfig);
            final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(null);
                connection = AwsUtils.createConnection(awsConfig);
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException e) {
                LOG.error("Can't create AWS SQS Producer", e);
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
    }

    private MessageProducer getProducer(String queueName) {
        if (!producers.containsKey(queueName)) {
            try {
                AwsUtils.ensureQueueExists(connection, queueName);
                MessageProducer producer = session.createProducer(session.createQueue(queueName));
                producers.put(queueName, producer);
            } catch (JMSException e) {
                LOG.error("Can't create producer", e);
            }
        }
        return producers.get(queueName);
    }

    /**
     * Register JMX reporter.
     */
    private void registerJmxReporter() {
        if (reporter == null) {
            reporter = JmxReporter.forRegistry(getMetrics())
                    .inDomain(AwsLocalMessageDispatcherFactory.class.getPackage().getName())
                    .build();
            reporter.start();
        }
    }

    /**
     * Destroy.
     */
    public void destroy() {
        if (reporter != null) {
            reporter.close();
            reporter = null;
        }

        if (!producers.isEmpty()) {
            producers.values().forEach(p -> {
                try {
                    p.close();
                } catch (JMSException e) {
                    LOG.error("Can't close producer.", e);
                }
            });
            producers.clear();
        }
    }

    /**
     * Sets the configuration administration.
     *
     * @param configAdmin the new configuration administration
     */
    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }
}
