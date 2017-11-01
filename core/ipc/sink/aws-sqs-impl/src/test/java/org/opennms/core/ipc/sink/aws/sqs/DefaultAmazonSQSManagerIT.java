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

package org.opennms.core.ipc.sink.aws.sqs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.SinkModule;

/**
 * The Class DefaultAmazonSQSManagerIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultAmazonSQSManagerIT {

    /**
     * Test queue name.
     */
    @Test
    public void testQueueName() {
        DefaultAmazonSQSManager mgr = new DefaultAmazonSQSManager();
        Properties awsConfig = new Properties();
        SinkModule<?, ?> module = mock(SinkModule.class);
        when(module.getId()).thenReturn("Heartbeat");
        Assert.assertEquals("OpenNMS-Sink-Heartbeat", mgr.getQueueName(awsConfig, module));
        awsConfig.put(AmazonSQSManager.AWS_QUEUE_NAME_PREFIX, "PROD");
        Assert.assertEquals("PROD-OpenNMS-Sink-Heartbeat", mgr.getQueueName(awsConfig, module));
        awsConfig.put(DefaultAmazonSQSManager.SqsQueueAttribute.FifoQueue.toString(), "true");
        Assert.assertEquals("PROD-OpenNMS-Sink-Heartbeat.fifo", mgr.getQueueName(awsConfig, module));
    }

    /**
     * Test queue attributes.
     */
    @Test
    public void testQueueAttributes() {
        DefaultAmazonSQSManager mgr = new DefaultAmazonSQSManager();
        Properties awsConfig = new Properties();
        awsConfig.put(AmazonSQSManager.AWS_REGION, "us-east-2");

        final int validDefaultAttributes = DefaultAmazonSQSManager.QUEUE_ATTRIBUTES_DEFAULTS.size() - 1; // FifoQueue=false should not be passed.
        Map<String,String> properties = mgr.buildProperties(awsConfig);
        Assert.assertEquals(validDefaultAttributes,  properties.size());

        awsConfig.put(DefaultAmazonSQSManager.SqsQueueAttribute.FifoQueue.toString(), "true");
        Assert.assertTrue(mgr.isFifoEnabled(awsConfig));
        Assert.assertFalse(mgr.isFifoContentDedupEnabled(awsConfig));
        properties = mgr.buildProperties(awsConfig);
        Assert.assertEquals(validDefaultAttributes + 2,  properties.size()); // FifoQueue was added as well as ContentBasedDeduplication
        final String dedup = DefaultAmazonSQSManager.SqsQueueAttribute.ContentBasedDeduplication.toString();
        Assert.assertEquals(DefaultAmazonSQSManager.CONTENT_BASED_DEDUP_DEFAULT,  properties.get(dedup));

        awsConfig.put(dedup, "true");
        Assert.assertTrue(mgr.isFifoContentDedupEnabled(awsConfig));

        awsConfig.put("Policy", "a-policy-here");
        properties = mgr.buildProperties(awsConfig);
        Assert.assertEquals(8,  properties.size());
        Assert.assertEquals("a-policy-here", properties.get("Policy"));
    }
}
