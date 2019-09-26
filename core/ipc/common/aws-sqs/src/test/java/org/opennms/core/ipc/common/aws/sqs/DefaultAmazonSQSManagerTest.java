/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.SystemInfoUtils;

import org.junit.Test;

/**
 * The Class DefaultAmazonSQSManagerTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultAmazonSQSManagerTest {

    /**
     * Test queue name.
     */
    @Test
    public void testQueueName() {
        String moduleId = "Heartbeat";
        Map<String, String> sqsConfig = new HashMap<>();
        DefaultAmazonSQSManager mgr = getManagerWith(sqsConfig);
        assertEquals(SystemInfoUtils.DEFAULT_INSTANCE_ID + "-Sink-Heartbeat", mgr.getSinkQueueName(moduleId));

        sqsConfig.put(AmazonSQSConstants.AWS_QUEUE_NAME_PREFIX, "PROD");
        mgr = getManagerWith(sqsConfig);
        assertEquals("PROD-" + SystemInfoUtils.DEFAULT_INSTANCE_ID + "-Sink-Heartbeat", mgr.getSinkQueueName(moduleId));

        sqsConfig.put(AmazonSQSConstants.SINK_QUEUE_PROP_PREFIX + AmazonSQSQueueAttributes.FifoQueue.toString(), "true");
        mgr = getManagerWith(sqsConfig);
        assertEquals("PROD-" + SystemInfoUtils.DEFAULT_INSTANCE_ID + "-Sink-Heartbeat.fifo", mgr.getSinkQueueName(moduleId));
    }

    /**
     * Test queue attributes.
     */
    @Test
    public void testQueueAttributes() {
        Map<String, String> sqsConfig = new HashMap<>();
        sqsConfig.put(AmazonSQSConstants.AWS_REGION, "us-east-2");

        // FifoQueue=false should not be passed.
        AmazonSQSQueueConfig queueConfig = getSinkQueueConfig(sqsConfig);
        Map<String,String> queueAttributes = queueConfig.getAttributes();
        assertThat(queueAttributes, not(hasKey(AmazonSQSQueueAttributes.FifoQueue.toString())));
        assertThat(queueAttributes, not(hasKey(AmazonSQSQueueAttributes.ContentBasedDeduplication.toString())));

        sqsConfig.put(AmazonSQSConstants.SINK_QUEUE_PROP_PREFIX + AmazonSQSQueueAttributes.FifoQueue.toString(), "true");
        queueConfig = getSinkQueueConfig(sqsConfig);
        queueAttributes = queueConfig.getAttributes();
        assertThat(queueConfig.isFifoEnabled(), equalTo(true));
        assertThat(queueConfig.isFifoContentDedupEnabled(), equalTo(false));
        // FifoQueue was added as well as ContentBasedDeduplication
        assertThat(queueAttributes, hasEntry(AmazonSQSQueueAttributes.FifoQueue.toString(), "true"));
        assertThat(queueAttributes, hasEntry(AmazonSQSQueueAttributes.ContentBasedDeduplication.toString(), "false"));

        sqsConfig.put(AmazonSQSConstants.SINK_QUEUE_PROP_PREFIX + AmazonSQSQueueAttributes.ContentBasedDeduplication.toString(), "true");
        queueConfig = getSinkQueueConfig(sqsConfig);
        queueAttributes = queueConfig.getAttributes();
        assertThat(queueConfig.isFifoEnabled(), equalTo(true));
        assertThat(queueConfig.isFifoContentDedupEnabled(), equalTo(true));
        // FifoQueue was added as well as ContentBasedDeduplication
        assertThat(queueAttributes, hasEntry(AmazonSQSQueueAttributes.FifoQueue.toString(), "true"));
        assertThat(queueAttributes, hasEntry(AmazonSQSQueueAttributes.ContentBasedDeduplication.toString(), "true"));

        // Another entry
        sqsConfig.put(AmazonSQSConstants.SINK_QUEUE_PROP_PREFIX + AmazonSQSQueueAttributes.Policy.toString(), "a-policy-here");
        queueConfig = getSinkQueueConfig(sqsConfig);
        queueAttributes = queueConfig.getAttributes();
        assertThat(queueAttributes, hasEntry(AmazonSQSQueueAttributes.Policy.toString(), "a-policy-here"));
    }

    private DefaultAmazonSQSManager getManagerWith(Map<String, String> sqsConfig) {
        AmazonSQSConfig config = new MapBasedSQSConfig(sqsConfig);
        return new DefaultAmazonSQSManager(config);
    }

    private AmazonSQSQueueConfig getSinkQueueConfig(Map<String, String> sqsConfig) {
        AmazonSQSConfig config = new MapBasedSQSConfig(sqsConfig);
        return config.getSinkQueueConfig();
    }
}
