/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.common.aws.sqs;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class DefaultAmazonSQSManager implements AmazonSQSManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAmazonSQSManager.class);

    private final AmazonSQSConfig sqsConfig;

    /**
     * Creates the SQS client in the first get.
     */
    private final Supplier<AmazonSQS> sqsClientSupplier = Suppliers.memoize(() -> createSQSClient());

    /**
     * Caches the RPC queue URL once created.
     */
    private LoadingCache<String, String> rpcQueueUrlsByName = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<String, String>() {
                        public String load(String queueName)  {
                            return ensureRpcQueueExists(queueName);
                        }
                    });

    /**
     * Caches the Sink queue URL once created.
     */
    private LoadingCache<String, String> sinkQueueUrlsById = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<String, String>() {
                public String load(String moduleId)  {
                    final String queueName = getSinkQueueName(moduleId);
                    return ensureSinkQueueExists(queueName);
                }
            });

    public DefaultAmazonSQSManager(AmazonSQSConfig sqsConfig) {
        this.sqsConfig = Objects.requireNonNull(sqsConfig);
    }

    @Override
    public String getSinkQueueUrlAndCreateIfNecessary(String moduleId) throws InterruptedException {
        while (true) {
            try {
                return sinkQueueUrlsById.get(moduleId);
            } catch (ExecutionException e) {
                if (isCause(UnknownHostException.class, e) || isCause(SocketException.class, e)) {
                    LOG.warn("Cannot reach AWS while trying to retrieve the queue details for module {}, trying again in 5 seconds...", moduleId);
                    Thread.sleep(5000);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String getRpcRequestQueueNameAndCreateIfNecessary(String moduleId, String location) throws AmazonSQSQueueException {
        final String rpcRequestQueueName = getRpcRequestQueueName(moduleId, location);
        try {
            rpcQueueUrlsByName.get(rpcRequestQueueName);
            return rpcRequestQueueName;
        } catch (ExecutionException e) {
            throw new AmazonSQSQueueException(e);
        }
    }

    @Override
    public String getRpcReplyQueueNameAndCreateIfNecessary(String moduleId, String location) throws AmazonSQSQueueException {
        final String rpcReplyQueueName = getRpcReplyQueueName(moduleId, location);
        try {
            rpcQueueUrlsByName.get(rpcReplyQueueName);
            return rpcReplyQueueName;
        } catch (ExecutionException e) {
            throw new AmazonSQSQueueException(e);
        }
    }

    @Override
    public AmazonSQS getSQSClient() {
        return sqsClientSupplier.get();
    }

    @Override
    public SQSConnectionFactory getSQSConnectionFactory() {
        return new SQSConnectionFactory(
                new ProviderConfiguration(),
                getSQSClient()
        );
    }

    @Override
    public String sendMessage(String queueUrl, String body) throws InterruptedException {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, body);
        if (sqsConfig.getSinkQueueConfig().isFifoEnabled()) {
            sendMessageRequest.setMessageGroupId(queueUrl);
            if (!sqsConfig.getSinkQueueConfig().isFifoContentDedupEnabled()) {
                sendMessageRequest.setMessageDeduplicationId(Long.toString(System.nanoTime()));
            }
        }
        while (true) {
            try {
                return getSQSClient().sendMessage(sendMessageRequest).getMessageId();
            } catch (RuntimeException ex) {
                if (isCause(UnknownHostException.class, ex) || isCause(SocketException.class, ex)) {
                    LOG.warn("Cannot reach AWS at {} while trying to send a message, trying again in 5 seconds...", queueUrl);
                    Thread.sleep(5000);
                } else {
                    throw ex;
                }
            }
        }
    }

    private AmazonSQS createSQSClient() {
        final AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
                .withRegion(sqsConfig.getRegion());
        if (sqsConfig.hasStaticCredentials()) {
            final BasicAWSCredentials awsCreds = new BasicAWSCredentials(sqsConfig.getAccessKey(), sqsConfig.getSecretKey());
            builder.withCredentials(new AWSStaticCredentialsProvider(awsCreds));
        }
        if (sqsConfig.isUseHttp()) {
            final ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.HTTP);
            builder.withClientConfiguration(clientConfig);
        }
        return builder.build();
    }

    private String getRpcRequestQueueName(String moduleId, String location) {
        String queueName = String.format("%s-%s-%s",
                SystemInfoUtils.getInstanceId(),
                moduleId,
                location);
        if (!Strings.isNullOrEmpty(sqsConfig.getQueuePrefix())) {
            queueName = String.format("%s-%s", sqsConfig.getQueuePrefix(), queueName);
        }
        return queueName;
    }

    private String getRpcReplyQueueName(String moduleId, String location) {
        return getRpcRequestQueueName(moduleId, location) + "-Reply";
    }

    protected String getSinkQueueName(String moduleId) {
        final String prefix = !Strings.isNullOrEmpty(sqsConfig.getQueuePrefix()) ? sqsConfig.getQueuePrefix() + "-" : "";
        final String suffix = sqsConfig.getSinkQueueConfig().isFifoEnabled() ? ".fifo" : "";
        return String.format("%s%s-%s-%s%s",
                prefix,
                SystemInfoUtils.getInstanceId(),
                "Sink",
                moduleId,
                suffix);
    }

    private String ensureRpcQueueExists(String queueName) {
        final Map<String, String> attributes = sqsConfig.getRpcQueueConfig().getAttributes();
        return ensureQueueExists(queueName, attributes);
    }

    private String ensureSinkQueueExists(String queueName) {
        final Map<String, String> attributes = sqsConfig.getSinkQueueConfig().getAttributes();
        return ensureQueueExists(queueName, attributes);
    }

    private String ensureQueueExists(String queueName, Map<String, String> attributes) {
        final AmazonSQS sqsClient = getSQSClient();
        try {
            return sqsClient.createQueue(new CreateQueueRequest(queueName).withAttributes(attributes)).getQueueUrl();
        } catch (QueueNameExistsException e) {
            final String queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
            sqsClient.setQueueAttributes(new SetQueueAttributesRequest(queueUrl, attributes));
            return queueUrl;
        }
    }

    /**
     * Checks if is cause.
     *
     * @param expected the expected
     * @param exc the exception
     * @return true, if is cause
     */
    private static boolean isCause(Class<? extends Throwable> expected, Throwable exc) {
        return expected.isInstance(exc) || (exc != null && isCause(expected, exc.getCause()));
    }

}
