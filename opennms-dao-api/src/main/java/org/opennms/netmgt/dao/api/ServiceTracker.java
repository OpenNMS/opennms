/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.io.Closeable;

/**
 * Tracking services is a pain - use this service to handle it for you.
 */
public interface ServiceTracker {

    interface ServiceListener {
        /**
         * Called when a service matches the criteria.
         *
         * @param serviceRef service reference
         */
        void onServiceMatched(ServiceRef serviceRef);

        /**
         * Called when a service that was previously passed to a {@link #onServiceMatched} call
         * no longer matches the criteria
         *
         * @param serviceRef service reference
         */
        void onServiceStoppedMatching(ServiceRef serviceRef);
    }

    /**
     * Issues callbacks to the given listener for services that:
     *   1) Have the given service name
     *   2) Match the given filter rule
     *
     * Callbacks are expected to be issued immediately for all existing services that match the criteria.
     * Additional callback will be made as services are added/removed.
     *
     * @param serviceName only interfaces with the given service name attached will be considered
     * @param filterRule only interfaces that match the given filter will be considered
     *                   if null, or empty the filter will match everything
     * @param listener used for callbacks
     * @return close when done watching
     */
    Closeable trackServiceMatchingFilterRule(String serviceName, String filterRule, ServiceListener listener);

    /**
     * Issues callbacks to the given listener for services that:
     *   1) Have the given service name
     *
     * Callbacks are expected to be issued immediately for all existing services that match the criteria.
     * Additional callback will be made as services are added/removed.
     *
     * @param serviceName only interfaces with the given service name attached will be considered
     * @param listener used for callbacks
     * @return close when done watching
     */
    Closeable trackService(String serviceName, ServiceListener listener);

}
