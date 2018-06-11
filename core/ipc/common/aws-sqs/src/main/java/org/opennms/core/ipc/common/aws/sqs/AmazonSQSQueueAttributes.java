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

import java.io.Serializable;

/**
 * An Enumeration for the SQS Queue Attributes.
 */
public enum AmazonSQSQueueAttributes implements Serializable {

    /** The Delay seconds. */
    DelaySeconds,

    /** The Maximum message size. */
    MaximumMessageSize,

    /** The Message retention period. */
    MessageRetentionPeriod,

    /** The Receive message wait time seconds. */
    ReceiveMessageWaitTimeSeconds,

    /** The Visibility timeout. */
    VisibilityTimeout,

    /** The FIFO queue. */
    FifoQueue,

    /** The Content based deduplication. */
    ContentBasedDeduplication,

    /** The Policy. */
    Policy,

    /** The Redrive policy. */
    RedrivePolicy,

    /** The KMS master key id. */
    KmsMasterKeyId,

    /** The KMS data key reuse period seconds. */
    KmsDataKeyReusePeriodSeconds
}
