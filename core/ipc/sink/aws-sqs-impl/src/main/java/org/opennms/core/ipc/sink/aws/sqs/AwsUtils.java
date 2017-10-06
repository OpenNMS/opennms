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

import java.util.Properties;

import javax.jms.JMSException;

import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.api.SinkModule;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * The Class AwsUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AwsUtils {

    /** The Constant AWS_REGION. */
    public static final String AWS_REGION = "aws_region";

    /** The Constant AWS_ACCESS_KEY_ID. */
    public static final String AWS_ACCESS_KEY_ID = "aws_access_key_id";

    /** The Constant AWS_SECRET_ACCESS_KEY. */
    public static final String AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";

    /**
     * Creates the SQS connection.
     *
     * @param awsConfig the AWS configuration
     * @return the SQS connection
     * @throws JMSException the JMS exception
     */
    public static SQSConnection createConnection(Properties awsConfig) throws JMSException {
        AWSCredentialsProvider credentialProvider = new ProfileCredentialsProvider();
        if (awsConfig.containsKey(AWS_ACCESS_KEY_ID) && awsConfig.containsKey(AWS_SECRET_ACCESS_KEY)) {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsConfig.getProperty(AWS_ACCESS_KEY_ID), awsConfig.getProperty(AWS_SECRET_ACCESS_KEY));
            credentialProvider = new AWSStaticCredentialsProvider(awsCreds);
        }
        AmazonSQSClientBuilder clientBuilder = AmazonSQSClientBuilder.standard()
                .withRegion(awsConfig.getProperty(AWS_REGION, Regions.US_EAST_1.getName()))
                .withCredentials(credentialProvider);
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(new ProviderConfiguration(), clientBuilder);
        return connectionFactory.createConnection();
    }

    /**
     * Ensure queue exists.
     *
     * @param connection the connection
     * @param queueName the queue name
     * @throws JMSException the JMS exception
     */
    public static void ensureQueueExists(SQSConnection connection, String queueName) throws JMSException {
        AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
        if (!client.queueExists(queueName)) {
            client.createQueue(queueName);
        }
    }

    /**
     * Gets the queue name.
     *
     * @param module the module
     * @return the queue name
     */
    public static String getQueueName(SinkModule<?, ?> module) {
        return new JmsQueueNameFactory(AwsSinkConstants.AWS_QUEUE_PREFIX, module.getId()).getName().replaceAll("\\.", "-");
    }

}