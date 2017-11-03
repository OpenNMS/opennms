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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSManager;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class AwsMessageConsumerManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AmazonSQSMessageConsumerManager extends AbstractMessageConsumerManager {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AmazonSQSMessageConsumerManager.class);

    /** The consumer runners by module. */
    private final Map<SinkModule<?, Message>, List<AwsConsumerRunner>> consumerRunnersByModule = new ConcurrentHashMap<>();

    /** The thread factory. */
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("sqs-consumer-%d")
            .build();

    /** The executor. */
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    /** The AWS SQS manager. */
    private AmazonSQSManager awsSqsManager;

    /**
     * The Class AwsConsumerRunner.
     */
    private class AwsConsumerRunner implements Runnable {

        /** The module. */
        private final SinkModule<?, Message> module;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The AWS SQS Object. */
        private final AmazonSQS sqs;

        /** The queue URL. This is cached to avoid an API call on each attempt to send a message. */
        private String queueUrl;

        /**
         * Instantiates a new AWS consumer runner.
         *
         * @param module the module
         * @throws AmazonSQSException the Amazon SQS exception
         */
        public AwsConsumerRunner(SinkModule<?, Message> module) throws AmazonSQSException {
            this.module = module;
            sqs = awsSqsManager.getSQSClient();
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            Logging.putPrefix(MessageConsumerManager.LOG_PREFIX);
            try {
                LOG.debug("Retrieving queue URL for module with id: {}", module.getId());
                queueUrl = awsSqsManager.getSinkQueueUrlAndCreateIfNecessary(module.getId());
                LOG.debug("Using queue URL: {}", queueUrl);
            } catch (InterruptedException e) {
                LOG.error("Interrupted while retrieving queue URL. Aborting consumer.", e);
            }

            while (!closed.get()) {
                // The SQS Queue is configured to use "Long Polling" through "ReceiveMessageWaitTimeSeconds".
                // That means, calling receiveMessage will block the thread execution for that amount of time if there are no messages on the queue.
                // This is recommended to reduce the traffic against AWS, which can be translated into undesired costs.
                List<com.amazonaws.services.sqs.model.Message> messages = null;
                try {
                    messages = sqs.receiveMessage(queueUrl).getMessages();
                } catch (RuntimeException e) {
                    LOG.error("Unexpected exception while receiving messages from " + queueUrl, e);
                }
                if (messages != null) {
                    for (com.amazonaws.services.sqs.model.Message m : messages) {
                        try {
                            LOG.debug("Received SQS message with ID {} from {}", m.getMessageId(), queueUrl);
                            final Message msg = module.unmarshal(m.getBody().getBytes(StandardCharsets.UTF_8));
                            dispatch(module, msg);
                            LOG.debug("Message with ID {} successfully dispatched.", m.getMessageId(), queueUrl);
                        } catch (RuntimeException e) {
                            final String msg = String.format("Unexpected exception while dispatching message with ID %s from %s", m.getMessageId(), queueUrl);
                            LOG.warn(msg, e);
                        } finally {
                            // This is mandatory to avoid re-process a message on a subsequent receive request.
                            LOG.debug("Deleting SQS message receipt handle {} from {}", m.getReceiptHandle(), queueUrl);
                            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
                        }
                    }
                }
            }
        }

        /**
         * Shutdown hook which can be called from a separate thread
         */
        public void shutdown() {
            closed.set(true);
            sqs.shutdown();
        }
    }

    /**
     * Sets the AWS SQS manager.
     *
     * @param awsSqsManager the new AWS SQS manager
     */
    public void setAwsSqsManager(AmazonSQSManager awsSqsManager) {
        this.awsSqsManager = awsSqsManager;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager#startConsumingForModule(org.opennms.core.ipc.sink.api.SinkModule)
     */
    @Override
    protected void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (!consumerRunnersByModule.containsKey(module)) {
            LOG.info("Starting consumers for module: {}", module);

            final int numConsumerThreads = getNumConsumerThreads(module);
            final List<AwsConsumerRunner> consumerRunners = new ArrayList<>(numConsumerThreads);
            for (int i = 0; i < numConsumerThreads; i++) {
                final AwsConsumerRunner consumerRunner = new AwsConsumerRunner(module);
                executor.execute(consumerRunner);
                consumerRunners.add(new AwsConsumerRunner(module));
            }

            consumerRunnersByModule.put(module, consumerRunners);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager#stopConsumingForModule(org.opennms.core.ipc.sink.api.SinkModule)
     */
    @Override
    protected void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (consumerRunnersByModule.containsKey(module)) {
            LOG.info("Stopping consumers for module: {}", module);
            final List<AwsConsumerRunner> consumerRunners = consumerRunnersByModule.get(module);
            for (AwsConsumerRunner consumerRunner : consumerRunners) {
                consumerRunner.shutdown();
            }
            consumerRunnersByModule.remove(module);
        }
    }
}
