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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.BmpMessageHandler;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapterFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BmpPersistingAdapterFactory extends AbstractAdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BmpPersistingAdapterFactory.class);

    private final BmpMessageHandler bmpMessageHandler;

    public BmpPersistingAdapterFactory(BmpMessageHandler bmpMessageHandler) {
        super(null);
        this.bmpMessageHandler = bmpMessageHandler;
    }

    public BmpPersistingAdapterFactory(BundleContext bundleContext, BmpMessageHandler bmpMessageHandler) {
        super(bundleContext);
        this.bmpMessageHandler = bmpMessageHandler;
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return BmpPersistingAdapter.class;
    }

    @Override
    public Adapter createBean(AdapterDefinition adapterConfig) {

        return new BmpPersistingAdapter(adapterConfig,
                this.getTelemetryRegistry().getMetricRegistry(),
                bmpMessageHandler);
    }

}
