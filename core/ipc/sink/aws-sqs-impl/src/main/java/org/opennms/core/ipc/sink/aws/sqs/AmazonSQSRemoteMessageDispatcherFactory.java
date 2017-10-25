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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
import com.amazonaws.services.sqs.model.SendMessageResult;
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

    /** The AWS SQS Object. */
    private AmazonSQS sqs;

    /** The AWS SQS manager. */
    private AmazonSQSManager awsSqsManager;

    /** The queue URL. This is cached to avoid an API call on each attempt to send a message. */
    private Map<String,String> queueUrls = new HashMap<>();

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#getModuleMetadata(org.opennms.core.ipc.sink.api.SinkModule)
     */
    @Override
    public <S extends Message, T extends Message> String getModuleMetadata(final SinkModule<S, T> module) {
        return awsSqsManager.getQueueName(awsConfig, module);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#dispatch(org.opennms.core.ipc.sink.api.SinkModule, java.lang.Object, org.opennms.core.ipc.sink.api.Message)
     */
    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String topic, T message) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.trace("dispatch({}): sending message {}", topic, message);
            final String queueName = awsSqsManager.getQueueName(awsConfig, module);
            final String queueUrl = getQueueUrl(queueName);
            if (queueUrl == null) {
                LOG.error("Cannot obtain URL for queue {}. The message cannot be sent.", queueName);
            } else {
                while (true) {
                    try {
                        final SendMessageResult result = sqs.sendMessage(queueUrl, module.marshal((T)message));
                        LOG.debug("SQS Message with ID {} has been successfully sent to {}", result.getMessageId(), queueUrl);
                        break;
                    } catch (RuntimeException ex) {
                        // Thanks to field tests, when a Minion cannot access AWS it throws a SdkClientException,
                        // with either UnknownHostException (DNS issues), or SocketException (server unreachable).
                        if (isCause(UnknownHostException.class, ex) || isCause(SocketException.class, ex)) {
                            LOG.warn("Cannot reach AWS at {} while trying to send a message, trying again in 5 seconds...", queueUrl);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {}
                        } else {
                            LOG.error("Unexpected AWS SDK exception while sending a message", ex);
                            break;
                        }
                    }
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

            // TODO we might need to obfuscate the credentials for security reasons.
            LOG.info("AwsRemoteMessageDispatcherFactory: initializing the AmazonSQS Object with: {}", awsConfig);
            try {
                sqs = awsSqsManager.createSQSObject(awsConfig);
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
        sqs.shutdown();
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
     * Sets the AWS SQS manager.
     *
     * @param awsSqsManager the new AWS SQS manager
     */
    public void setAwsSqsManager(AmazonSQSManager awsSqsManager) {
        this.awsSqsManager = awsSqsManager;
    }

    /**
     * Gets the queue URL.
     *
     * @param queueName the queue name
     * @return the queue URL
     */
    private String getQueueUrl(final String queueName) {
        if (queueUrls.containsKey(queueName)) return queueUrls.get(queueName);
        try {
            queueUrls.put(queueName, sqs.getQueueUrl(queueName).getQueueUrl());
        } catch (QueueDoesNotExistException e) {
            try {
                queueUrls.put(queueName, awsSqsManager.ensureQueueExists(awsConfig, sqs, queueName));
            } catch (AmazonSQSException ex) {
                LOG.error("Cannot create queue with name " + queueName, ex);
            }
        }
        return queueUrls.get(queueName);
    }

    /**
     * Checks if is cause.
     *
     * @param expected the expected
     * @param exc the exception
     * @return true, if is cause
     */
    private boolean isCause(Class<? extends Throwable> expected, Throwable exc) {
        return expected.isInstance(exc) || (exc != null && isCause(expected, exc.getCause()));
    }

}
