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

package org.opennms.netmgt.telemetry.config.api;

import java.util.List;
import java.util.Optional;

/**
 * Telemetry protocol configuration.
 */
public interface Protocol {

    /**
     * The name of the protocol.
     *
     * This is used as a suffix for any associated queues that are created and
     * must be the same on both OpenNMS and Minion.
     *
     * @return the protocol name
     */
    String getName();

    /**
     * Number of threads used for consuming/dispatching messages.
     *
     * @return the number of threads
     */
    Optional<Integer> getNumThreads();

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch reaches this size, it will be dispatched.
     *
     * @return the batch size
     */
    Optional<Integer> getBatchSize();

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch has been created for longer than this interval (ms)
     * it will be dispatched, regardless of the current size.
     *
     * @return the batch interval
     */
    Optional<Integer> getBatchIntervalMs();

    /**
     * Maximum number of messages to keep in memory while waiting
     * to be dispatched.
     *
     * @return the queue size
     */
    Optional<Integer> getQueueSize();

    /**
     * Packages may contain settings for specific sources.
     *
     * @return the list of configured packages
     */
    List<? extends Package> getPackages();
}
