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

package org.opennms.features.grpc.exporter.mapper;

import org.opennms.integration.api.v1.model.IpInterface;
import org.opennms.integration.api.v1.model.MonitoredService;
import org.opennms.integration.api.v1.model.Node;

import java.util.Objects;

public class MonitoredServiceWithMetadata {

    private final Node node;
    private final IpInterface iface;
    private final MonitoredService monitoredService;

    public MonitoredServiceWithMetadata(final Node node,
                                        final IpInterface iface,
                                        final MonitoredService monitoredService) {
        this.node = Objects.requireNonNull(node);
        this.iface = Objects.requireNonNull(iface);
        this.monitoredService = Objects.requireNonNull(monitoredService);
    }

    public Node getNode() {
        return this.node;
    }

    public IpInterface getIface() {
        return this.iface;
    }

    public MonitoredService getMonitoredService() {
        return this.monitoredService;
    }
}
