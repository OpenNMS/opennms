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

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.ipc.sink.api.SinkModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

/**
 * The Class DefaultAmazonSQSManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultAmazonSQSManager implements AmazonSQSManager {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAmazonSQSManager.class);

    /** The Constant FIFO_CONTENT_DEDUP. */
    private static final String FIFO_CONTENT_DEDUP = "ContentBasedDeduplication";

    static {
        QUEUE_ATTRIBUTES.put("DelaySeconds", "0");
        QUEUE_ATTRIBUTES.put("MaximumMessageSize", "262144");
        QUEUE_ATTRIBUTES.put("MessageRetentionPeriod", "1209600");
        QUEUE_ATTRIBUTES.put("ReceiveMessageWaitTimeSeconds", "10");
        QUEUE_ATTRIBUTES.put("VisibilityTimeout", "30");
        QUEUE_ATTRIBUTES.put("Policy", null);
        QUEUE_ATTRIBUTES.put("RedrivePolicy", null);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#createSQSObject(java.util.Properties)
     */
    public AmazonSQS createSQSObject(Properties awsConfig) throws AmazonSQSException {
        AWSCredentialsProvider credentialProvider;
        if (awsConfig.containsKey(AWS_ACCESS_KEY_ID) && awsConfig.containsKey(AWS_SECRET_ACCESS_KEY)) {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsConfig.getProperty(AWS_ACCESS_KEY_ID), awsConfig.getProperty(AWS_SECRET_ACCESS_KEY));
            credentialProvider = new AWSStaticCredentialsProvider(awsCreds);
        } else {
            credentialProvider = new ProfileCredentialsProvider();
        }
        return AmazonSQSClientBuilder.standard()
                .withRegion(awsConfig.getProperty(AWS_REGION, Regions.US_EAST_1.getName()))
                .withCredentials(credentialProvider)
                .build();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#ensureQueueExists(java.util.Properties, com.amazonaws.services.sqs.AmazonSQS, java.lang.String)
     */
    public String ensureQueueExists(Properties awsConfig, AmazonSQS sqs, String queueName) throws AmazonSQSException {
        final Map<String,String> attributes = new HashMap<>();
        QUEUE_ATTRIBUTES.forEach((k,v) -> {
            final String val = awsConfig.getProperty(k,v);
            if (val != null) attributes.put(k, val);
        });
        if (isFifoEnabled(awsConfig)) {
            attributes.put("FifoQueue", "true");
            attributes.put(FIFO_CONTENT_DEDUP, awsConfig.getProperty(FIFO_CONTENT_DEDUP, "false"));
        }
        try {
            return sqs.createQueue(new CreateQueueRequest(queueName).withAttributes(attributes)).getQueueUrl();
        } catch (AmazonSQSException e) {
            if (e.getErrorCode().equals("QueueAlreadyExists")) {
                final String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
                sqs.setQueueAttributes(new SetQueueAttributesRequest(queueUrl, attributes));
                return queueUrl;
            } else {
                throw e;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#getQueueName(java.util.Properties, org.opennms.core.ipc.sink.api.SinkModule)
     */
    public String getQueueName(Properties awsConfig, SinkModule<?, ?> module) {
        final String prefix = awsConfig.containsKey(AWS_QUEUE_NAME_PREFIX) ? awsConfig.getProperty(AWS_QUEUE_NAME_PREFIX) + '-' : "";
        final String suffix = isFifoEnabled(awsConfig) ? ".fifo" : "";
        return prefix + AmazonSQSSinkConstants.AWS_QUEUE_PREFIX + '-' + module.getId() + suffix;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#sendMessage(java.util.Properties, com.amazonaws.services.sqs.AmazonSQS, java.lang.String, java.lang.String)
     */
    @Override
    public String sendMessage(Properties awsConfig, AmazonSQS sqs, String queueUrl, String body) throws RuntimeException {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, body);
        if (isFifoEnabled(awsConfig)) {
            sendMessageRequest.setMessageGroupId(queueUrl);
            if (!isFifoContentDedupEnabled(awsConfig)) {
                sendMessageRequest.setMessageDeduplicationId(Long.toString(System.nanoTime()));
            }
        }
        while (true) {
            try {
                return sqs.sendMessage(sendMessageRequest).getMessageId();
            } catch (RuntimeException ex) {
                if (isCause(UnknownHostException.class, ex) || isCause(SocketException.class, ex)) {
                    LOG.warn("Cannot reach AWS at {} while trying to send a message, trying again in 5 seconds...", queueUrl);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {}
                } else {
                    throw ex;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#receiveMessages(java.util.Properties, com.amazonaws.services.sqs.AmazonSQS, java.lang.String)
     */
    @Override
    public List<Message> receiveMessages(Properties awsConfig, AmazonSQS sqs, String queueUrl) throws RuntimeException {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        if (isFifoEnabled(awsConfig)) {
            // In the future, in case we want to retry read operations, we should use the same ID on subsequents requests
            receiveMessageRequest.setReceiveRequestAttemptId(Long.toString(System.nanoTime()));
        }
        return sqs.receiveMessage(receiveMessageRequest).getMessages();
    }

    /**
     * Checks if is FIFO enabled.
     *
     * @param awsConfig the AWS configuration
     * @return true, if is FIFO enabled
     */
    private boolean isFifoEnabled(Properties awsConfig) {
        return Boolean.parseBoolean(awsConfig.getProperty(AWS_USE_FIFO_QUEUE, "false"));
    }

    /**
     * Checks if is FIFO content deduplication enabled.
     *
     * @param awsConfig the AWS configuration
     * @return true, if is content deduplication enabled
     */
    private boolean isFifoContentDedupEnabled(Properties awsConfig) {
        return Boolean.parseBoolean(awsConfig.getProperty(FIFO_CONTENT_DEDUP, "false"));
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
