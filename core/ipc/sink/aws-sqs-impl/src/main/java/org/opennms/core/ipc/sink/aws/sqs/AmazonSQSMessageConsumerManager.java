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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.amazonaws.services.sqs.AmazonSQS;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * The Class AwsMessageConsumerManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AmazonSQSMessageConsumerManager extends AbstractMessageConsumerManager implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AmazonSQSMessageConsumerManager.class);

    /** The consumer runners by module. */
    private final Map<SinkModule<?, Message>, List<AwsConsumerRunner>> consumerRunnersByModule = new ConcurrentHashMap<>();

    /** The thread factory. */
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("aws-consumer-%d")
            .build();

    /** The executor. */
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    /** The AWS configuration. */
    private final Properties awsConfig = new Properties();

    /**
     * The Class AwsConsumerRunner.
     */
    private class AwsConsumerRunner implements Runnable {

        /** The module. */
        private final SinkModule<?, Message> module;

        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The SQS Object. */
        private final AmazonSQS sqs;

        /**
         * Instantiates a new AWS consumer runner.
         *
         * @param module the module
         * @throws JMSException 
         */
        public AwsConsumerRunner(SinkModule<?, Message> module) throws JMSException {
            this.module = module;
            sqs = AmazonSQSUtils.createSQSObject(awsConfig);
            AmazonSQSUtils.ensureQueueExists(awsConfig, sqs, AmazonSQSUtils.getQueueName(module));
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            Logging.putPrefix(MessageConsumerManager.LOG_PREFIX);
            while (!closed.get()) {
                final String queueUrl = sqs.getQueueUrl(AmazonSQSUtils.getQueueName(module)).getQueueUrl();
                sqs.receiveMessage(queueUrl).getMessages().forEach(m -> {
                    LOG.debug("Got message {}", m.getMessageId());
                    try {
                        final Message msg = module.unmarshal(m.getBody());
                        dispatch(module, msg);
                        sqs.deleteMessage(queueUrl, m.getReceiptHandle());
                    } catch (RuntimeException e) {
                        LOG.warn("Unexpected exception while dispatching message", e);
                    }
                });
            }
        }

        /**
         * Shutdown hook which can be called from a separate thread
         */
        public void shutdown() {
            closed.set(true);
        }
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

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // Set the defaults
        awsConfig.clear();

        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null ||  !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String)keyAsObject;

            if (key.length() > AmazonSQSSinkConstants.AWS_CONFIG_SYS_PROP_PREFIX.length()
                    && key.startsWith(AmazonSQSSinkConstants.AWS_CONFIG_SYS_PROP_PREFIX)) {
                final String awsConfigKey = key.substring(AmazonSQSSinkConstants.AWS_CONFIG_SYS_PROP_PREFIX.length());
                awsConfig.put(awsConfigKey, entry.getValue());
            }
        }
        LOG.info("AwsMessageConsumerManager: consuming from AWS SQS using: {}", awsConfig);
    }
}
