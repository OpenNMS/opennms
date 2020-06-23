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

import com.amazonaws.regions.Regions;

/**
 * Encapsulates all the SQS related bits that can be configured.
 */
public interface AmazonSQSConfig {

    /**
     * Prefix to be added to all queues.
     *
     * This can be used to differentiate between OpenNMS deployments in a single region.
     *
     * @return the prefix, or null if no prefix should be used.
     */
    String getQueuePrefix();

    /**
     * AWS region to use.
     *
     * @return the region, or null if the default region should be used.
     */
    Regions getRegion();

    /**
     * AWS access key.
     *
     * @return the key, or null if the client should be initialized without static credentials.
     */
    String getAccessKey();

    /**
     * AWS secret key.
     *
     * @return the key, or null if the client should be initialized without static credentials.
     */
    String getSecretKey();

    /**
     * Convenience method that returns <code>true</code> if both an access key and secret key have been set.
     *
     * @return <code>true</code> if both an access key and secret key have been set, <code>false</code> otherwise.
     */
    boolean hasStaticCredentials();

    /**
     * Used to force the SQS client to use HTTP instead of HTTPS.
     *
     * This can be used for testing, making it easier to intercept the messages sent over the wire.
     *
     * @return <code>true</code> if HTTP should be used insetad of HTTPS, <code>false</code> otherwise.
     */
    boolean isUseHttp();

    /**
     * Retrieves the queue configuration for queues used by the Sink API.
     *
     * @return the queue configuration
     */
    AmazonSQSQueueConfig getSinkQueueConfig();

    /**
     * Retrieves the queue configuration for queues used by the RPC API.
     *
     * @return the queue configuration
     */
    AmazonSQSQueueConfig getRpcQueueConfig();

}
