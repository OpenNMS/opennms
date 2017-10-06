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
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * The Class AwsMessageConsumerManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AwsMessageConsumerManager extends AbstractMessageConsumerManager implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AwsMessageConsumerManager.class);

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

        /** The consumer. */
        private final MessageConsumer consumer;

        /** The connection. */
        private final SQSConnection connection;

        /**
         * Instantiates a new AWS consumer runner.
         *
         * @param module the module
         * @throws JMSException the JMS exception
         */
        public AwsConsumerRunner(SinkModule<?, Message> module) throws JMSException {
            this.module = module;
            final String queueName = AwsUtils.getQueueName(module);
            connection = AwsUtils.createConnection(awsConfig);
            AwsUtils.ensureQueueExists(connection, queueName);
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue(queueName));
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            Logging.putPrefix(MessageConsumerManager.LOG_PREFIX);
            try {
                connection.start();
                while (!closed.get()) {
                    javax.jms.Message message = consumer.receive(30000);
                    LOG.debug("Got message {}", message.getJMSMessageID());
                    if (message instanceof TextMessage) {
                        TextMessage txtMessage = (TextMessage) message;
                        try {
                            dispatch(module, module.unmarshal(txtMessage.getText()));
                        } catch (RuntimeException e) {
                            LOG.warn("Unexpected exception while dispatching message", e);
                        }
                    } else {
                        LOG.warn("Received an unsupported message {}", message.getJMSMessageID());
                    }
                }
            } catch (JMSException ex) {
                LOG.error("Can't receive message", ex);
            } finally {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    LOG.warn("Can't close consumer", e);
                }
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

            if (key.length() > AwsSinkConstants.AWS_CONFIG_SYS_PROP_PREFIX.length()
                    && key.startsWith(AwsSinkConstants.AWS_CONFIG_SYS_PROP_PREFIX)) {
                final String awsConfigKey = key.substring(AwsSinkConstants.AWS_CONFIG_SYS_PROP_PREFIX.length());
                awsConfig.put(awsConfigKey, entry.getValue());
            }
        }
        LOG.info("AwsMessageConsumerManager: consuming from AWS SQS using: {}", awsConfig);
    }
}
