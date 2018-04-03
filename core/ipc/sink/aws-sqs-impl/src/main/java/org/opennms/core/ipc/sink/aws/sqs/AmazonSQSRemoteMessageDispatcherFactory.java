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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.codahale.metrics.JmxReporter;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSManager;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A factory for creating AwsRemoteMessageDispatcher objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AmazonSQSRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<String> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AmazonSQSRemoteMessageDispatcherFactory.class);

    /** The reporter. */
    private JmxReporter reporter;

    /** The AWS SQS Object. */
    private AmazonSQS sqs;

    /** The AWS SQS manager. */
    private AmazonSQSManager awsSqsManager;

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#dispatch(org.opennms.core.ipc.sink.api.SinkModule, java.lang.Object, org.opennms.core.ipc.sink.api.Message)
     */
    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String topic, T message) {
        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.trace("dispatch({}): sending message {}", topic, message);
            try {
                final String queueUrl = awsSqsManager.getSinkQueueUrlAndCreateIfNecessary(module.getId());
                final String messageId = awsSqsManager.sendMessage(queueUrl, new String(module.marshal((T)message), StandardCharsets.UTF_8));
                LOG.debug("SQS Message with ID {} has been successfully sent to {}", messageId, queueUrl);
            } catch (InterruptedException ex) {
                LOG.warn("Interrupted while trying to send message. Aborting.", ex);
            } catch (RuntimeException ex) {
                LOG.error("Unexpected AWS SDK exception while sending a message. Aborting.", ex);
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

            try {
                sqs = awsSqsManager.getSQSClient();
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
     * Sets the AWS SQS manager.
     *
     * @param awsSqsManager the new AWS SQS manager
     */
    public void setAwsSqsManager(AmazonSQSManager awsSqsManager) {
        this.awsSqsManager = awsSqsManager;
    }

}
