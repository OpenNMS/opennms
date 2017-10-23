/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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
package org.opennms.core.ipc.sink.aws.sqs.heartbeat;

import java.util.Properties;

import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager;
import org.opennms.core.ipc.sink.aws.sqs.AmazonSQSSinkConstants;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;

/**
 * The Class MockAmazonSQSManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockAmazonSQSManager implements AmazonSQSManager {

    /** The Constant URL_PREFIX. */
    public final static String URL_PREFIX = "http://mocksqs/";

    /** The Mock SQS Object. */
    private MockAmazonSQS sqs = new MockAmazonSQS();

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#createSQSObject(java.util.Properties)
     */
    @Override
    public AmazonSQS createSQSObject(Properties awsConfig) throws AmazonSQSException {
        return getSqsObject();
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#ensureQueueExists(java.util.Properties, com.amazonaws.services.sqs.AmazonSQS, java.lang.String)
     */
    @Override
    public String ensureQueueExists(Properties awsConfig, AmazonSQS sqs, String queueName) throws AmazonSQSException {
        return URL_PREFIX + queueName;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.aws.sqs.AmazonSQSManager#getQueueName(java.util.Properties, org.opennms.core.ipc.sink.api.SinkModule)
     */
    @Override
    public String getQueueName(Properties awsConfig, SinkModule<?, ?> module) {
        return AmazonSQSSinkConstants.AWS_QUEUE_PREFIX + '-' + module.getId();
    }

    /**
     * Gets the SQS object.
     *
     * @return the SQS object
     */
    public AmazonSQS getSqsObject() {
        return sqs;
    }

}
