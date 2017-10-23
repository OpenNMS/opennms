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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;

/**
 * The Interface AmazonSQSManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public interface AmazonSQSManager {

    /** The Constant AWS_REGION. */
    public static final String AWS_REGION = "aws_region";

    /** The Constant AWS_ACCESS_KEY_ID. */
    public static final String AWS_ACCESS_KEY_ID = "aws_access_key_id";

    /** The Constant AWS_SECRET_ACCESS_KEY. */
    public static final String AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";

    /** The Constant AWS_QUEUE_NAME_PREFIX. */
    public static final String AWS_QUEUE_NAME_PREFIX = "aws_queue_name_prefix";

    /** The Constant QUEUE_ATTRIBUTES. */
    public static final Map<String, String> QUEUE_ATTRIBUTES = new HashMap<>(); 

    /**
     * Creates the SQS object.
     *
     * @param awsConfig the AWS configuration
     * @return the amazon SQS
     * 
     * @throws AmazonSQSException the Amazon SQS exception
     */
    AmazonSQS createSQSObject(Properties awsConfig) throws AmazonSQSException;

    /**
     * Ensure queue exists.
     * <p>Tries to create the queue. If it already exist, update the settings based awsConfig.</p> 
     *
     * @param awsConfig the AWS configuration
     * @param sqs the SQS Object
     * @param queueName the queue name
     * @return the Queue URL
     * 
     * @throws AmazonSQSException the Amazon SQS exception
     */
    String ensureQueueExists(Properties awsConfig, AmazonSQS sqs, String queueName) throws AmazonSQSException;

    /**
     * Gets the queue name.
     *
     * @param awsConfig the AWS configuration
     * @param module the Sink module
     * @return the queue name
     */
    String getQueueName(Properties awsConfig, SinkModule<?, ?> module);

}
