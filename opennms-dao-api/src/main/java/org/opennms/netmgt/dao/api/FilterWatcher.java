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
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Evaluates filter rule and issues callback when the results change.
 */
public interface FilterWatcher {

    interface FilterResults {
        Map<Integer, Map<InetAddress, Set<String>>> getNodeIpServiceMap();

        Set<ServiceRef> getServicesNamed(String serviceName);
    }

    /**
     * Issues callbacks to the given consumer when the results of the filter change.
     *
     * A callback is expected to be issued immediately when the watch session is started.
     *
     * Additional callback will be made if/when the results change.
     *
     * @param filterRule a valid filter rule
     *                   if null, or empty the filter will match everything
     * @param callback used for callbacks
     * @return close when done watching
     */
    Closeable watch(String filterRule, Consumer<FilterResults> callback);

}