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
package org.opennms.core.daemon.loader;

import java.util.concurrent.Executor;

import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollerRequestBuilder;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;

/**
 * Local LocationAwarePollerClient for standalone daemon containers.
 * Executes all polls in-process without RPC to Minion.
 */
public class LocalPollerClient implements LocationAwarePollerClient {

    private final ServiceMonitorRegistry registry;
    private final Executor executor;

    public LocalPollerClient(ServiceMonitorRegistry registry, Executor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @Override
    public PollerRequestBuilder poll() {
        return new LocalPollerRequestBuilder(registry, executor);
    }
}
