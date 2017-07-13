/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.List;

import org.opennms.netmgt.snmp.SnmpV3User;

public interface TrapdConfig {
	String getSnmpTrapAddress();
	
    int getSnmpTrapPort();

    /**
     * Whether or not a newSuspect event should be generated with a trap from an
     * unknown IP address
     */
    boolean getNewSuspectOnTrap();

    List<SnmpV3User> getSnmpV3Users();

    boolean isIncludeRawMessage();

    /**
     * Number of threads used for consuming/dispatching messages.
     *
     * @return number of threads
     */
    int getNumThreads();

    /**
     * Maximum number of messages to keep in memory while waiting
     * to be dispatched.
     *
     * @return queue size
     */
    int getQueueSize();

    /**
     * Messages are aggregated in batches before being dispatched.
     *
     * When the batch reaches this size, it will be dispatched.
     *
     * @return batch size
     */
    int getBatchSize();

    /**
     * Messages are aggregated in batches before being dispatched.
     *
     * When the batch has been created for longer than this interval
     * it will be dispatched, regardless of the size.
     *
     * @return interval in ms
     */
    int getBatchIntervalMs();

    void update(TrapdConfig config);

    boolean shouldUseAddressFromVarbind();
}
