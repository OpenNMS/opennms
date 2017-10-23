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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.ipc.sink.api.SinkModule;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

/**
 * The Class DefaultAmazonSQSManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultAmazonSQSManager implements AmazonSQSManager {

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
        AWSCredentialsProvider credentialProvider = new ProfileCredentialsProvider();
        if (awsConfig.containsKey(AWS_ACCESS_KEY_ID) && awsConfig.containsKey(AWS_SECRET_ACCESS_KEY)) {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsConfig.getProperty(AWS_ACCESS_KEY_ID), awsConfig.getProperty(AWS_SECRET_ACCESS_KEY));
            credentialProvider = new AWSStaticCredentialsProvider(awsCreds);
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
        final String prefix =  awsConfig.containsKey(AWS_QUEUE_NAME_PREFIX)  ? awsConfig.getProperty(AmazonSQSSinkConstants.AWS_QUEUE_PREFIX) + '-' : "";
        return prefix + AmazonSQSSinkConstants.AWS_QUEUE_PREFIX + '-' + module.getId();
    }

}
