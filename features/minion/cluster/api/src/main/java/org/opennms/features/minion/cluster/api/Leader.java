/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.minion.cluster.api;

import java.io.Closeable;

import org.opennms.distributed.core.api.MinionIdentity;

/**
 * Leader election service.
 *
 * Allows to pick one and only one minion in a location as a designated leader.
 */
public interface Leader {

    /**
     * Checks whether the executing minion is elected leader.
     *
     * @return {@code true} if this minion is leader, {@code false} otherwise
     */
    public boolean isLeader();

    /**
     * Watches for leader change.
     *
     * The listener is called everytime leadership of this minion changes.
     * After subscription, the consumer is immediately called to signal the current leadership.
     *
     * @param listener the listener to be called, whenever there is a change in leadership
     * @return A {@link Closeable} which cancels the subscription, when closed.
     */
    public Closeable watch(final Listener listener);

    /**
     * Retire the leadership.
     *
     * If this minion is elected as leader, it will relinquish and the cluster will elect a different leader.
     * This minion will decline future leadership selection.
     */
    public void retire();

    /**
     * Listener for changes in leadership.
     *
     * Use {@link #watch(Listener)} to register listeners.
     */
    public static interface Listener {
        /**
         * Callback method invoked when this minion is elected leader.
         */
        public void onGranted();

        /**
         * Callback method invoked when this minion is no longer leader.
         */
        public void onRevoked();
    }
}
