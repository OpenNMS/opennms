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
package org.opennms.netmgt.telemetry.protocols.collection;

import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.telemetry.api.adapter.AdapterFactory;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAdapterFactory implements AdapterFactory {
    protected final BundleContext bundleContext;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Autowired
    private TelemetryRegistry telemetryRegistry;

    public AbstractAdapterFactory(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public InterfaceToNodeCache getInterfaceToNodeCache() {
        return this.interfaceToNodeCache;
    }

    public void setInterfaceToNodeCache(final InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public TelemetryRegistry getTelemetryRegistry() {
        return this.telemetryRegistry;
    }

    public void setTelemetryRegistry(final TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }
}
