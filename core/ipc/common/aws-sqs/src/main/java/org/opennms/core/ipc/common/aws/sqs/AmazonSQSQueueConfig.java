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

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class AmazonSQSQueueConfig {

    public static final ImmutableMap<String, String> DEFAULT_QUEUE_ATTRIBUTES = new ImmutableMap.Builder<String, String>()
            .put(AmazonSQSQueueAttributes.DelaySeconds.toString(), "0")
            .put(AmazonSQSQueueAttributes.MaximumMessageSize.toString(), "262144")
            .put(AmazonSQSQueueAttributes.MessageRetentionPeriod.toString(), "1209600")
            .put(AmazonSQSQueueAttributes.ReceiveMessageWaitTimeSeconds.toString(), "10")
            .put(AmazonSQSQueueAttributes.VisibilityTimeout.toString(), "30")
            .put(AmazonSQSQueueAttributes.FifoQueue.toString(), "false")
            .put(AmazonSQSQueueAttributes.ContentBasedDeduplication.toString(), "false")
            .build();

    private final Map<String, String> attributes;

    public AmazonSQSQueueConfig(Map<String, String> sqsConfig) {
        final Map<String,String> attributes = new HashMap<>();
        for (AmazonSQSQueueAttributes attr : AmazonSQSQueueAttributes.values()) {
            // Retrieve the property, using the default if provided
            final String val = sqsConfig.getOrDefault(attr.toString(), DEFAULT_QUEUE_ATTRIBUTES.get(attr.toString()));
            if (val != null) {
                attributes.put(attr.toString(), val);
            }
        }
        if (!Boolean.parseBoolean(attributes.get(AmazonSQSQueueAttributes.FifoQueue.toString()))) {
            attributes.remove(AmazonSQSQueueAttributes.FifoQueue.toString());
            attributes.remove(AmazonSQSQueueAttributes.ContentBasedDeduplication.toString());
        }
        this.attributes = ImmutableMap.copyOf(attributes);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean isFifoEnabled() {
        return Boolean.parseBoolean(attributes.get(AmazonSQSQueueAttributes.FifoQueue.toString()));
    }

    public boolean isFifoContentDedupEnabled() {
        return Boolean.parseBoolean(attributes.get(AmazonSQSQueueAttributes.ContentBasedDeduplication.toString()));

    }

    @Override
    public String toString() {
        return "AmazonSQSQueueConfig{" +
                "attributes=" + attributes +
                '}';
    }
}
