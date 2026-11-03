/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.api;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;

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
     * A {@link ServiceListener} that receives every change in the tracked results as a single call.
     */
    interface BatchServiceListener extends ServiceListener {
        /**
         * Called once per change in the tracked results, with every service that started or
         * stopped matching in that change.
         *
         * If this throws, none of the changes are considered delivered and they are passed again
         * with the next change, so implementations must tolerate services they already handled.
         *
         * @param matched services that now match the criteria
         * @param stoppedMatching services that no longer match the criteria
         */
        void onServicesChanged(Set<ServiceRef> matched, Set<ServiceRef> stoppedMatching);

        @Override
        default void onServiceMatched(ServiceRef serviceRef) {
            onServicesChanged(Collections.singleton(serviceRef), Collections.emptySet());
        }

        @Override
        default void onServiceStoppedMatching(ServiceRef serviceRef) {
            onServicesChanged(Collections.emptySet(), Collections.singleton(serviceRef));
        }
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
