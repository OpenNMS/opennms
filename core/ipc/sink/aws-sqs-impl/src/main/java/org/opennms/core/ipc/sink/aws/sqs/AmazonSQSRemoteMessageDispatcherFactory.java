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
import java.util.Properties;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.codahale.metrics.JmxReporter;

/**
 * A factory for creating AwsRemoteMessageDispatcher objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AmazonSQSRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<String> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AmazonSQSRemoteMessageDispatcherFactory.class);

    /** The AWS configuration. */
    private final Properties awsConfig = new Properties();

    /** The configuration administration object. */
    private ConfigurationAdmin configAdmin;

    /** The reporter. */
    private JmxReporter reporter;

    /** The SQS Object. */
    private AmazonSQS sqs;

    /** The queue URL. This is cached to avoid an API call on each attempt to send a message. */
    private String queueUrl;

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#getModuleMetadata(org.opennms.core.ipc.sink.api.SinkModule)
     */
    @Override
    public <S extends Message, T extends Message> String getModuleMetadata(final SinkModule<S, T> module) {
        return AmazonSQSUtils.getQueueName(module);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#dispatch(org.opennms.core.ipc.sink.api.SinkModule, java.lang.Object, org.opennms.core.ipc.sink.api.Message)
     */
    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String topic, T message) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.trace("dispatch({}): sending message {}", topic, message);
            final String queueName = getModuleMetadata(module);
            final String queueUrl = getQueueUrl(queueName);
            if (queueUrl == null) {
                LOG.warn("Cannot obtain URL for queue {}. The message cannot be sent.", queueName);
            } else {
                try {
                    sqs.sendMessage(queueUrl, module.marshal((T)message));
                } catch (RuntimeException e) {
                    LOG.warn("Unexpected exception while sending a message", e);
                }
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
            final Dictionary<String, Object> properties = configAdmin.getConfiguration(AmazonSQSSinkConstants.AWS_CONFIG_PID).getProperties();
            if (properties != null) {
                final Enumeration<String> keys = properties.keys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    awsConfig.put(key, properties.get(key));
                }
            }

            LOG.info("AwsRemoteMessageDispatcherFactory: initializing the AmazonSQS Object with: {}", awsConfig);
            try {
                sqs = AmazonSQSUtils.createSQSObject(awsConfig);
            } catch (AmazonSQSException e) {
                LOG.error("Can't create an AmazonSQS Object", e);
            }
        }
    }

    /**
     * Register JMX reporter.
     */
    private void registerJmxReporter() {
        if (reporter == null) {
            reporter = JmxReporter.forRegistry(getMetrics())
                    .inDomain(AmazonSQSLocalMessageDispatcherFactory.class.getPackage().getName())
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
    }

    /**
     * Sets the configuration administration.
     *
     * @param configAdmin the new configuration administration
     */
    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    /**
     * Gets the queue URL.
     *
     * @param queueName the queue name
     * @return the queue URL
     */
    private String getQueueUrl(final String queueName) {
        if (queueUrl != null) return queueUrl;
        try {
            queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        } catch (QueueDoesNotExistException e) {
            try {
                AmazonSQSUtils.ensureQueueExists(awsConfig, sqs, queueName);
                queueUrl= sqs.getQueueUrl(queueName).getQueueUrl();
            } catch (AmazonSQSException ex) {
                LOG.error("Cannot create queue with name " + queueName, ex);
            }
        }
        return queueUrl;
    }
}
