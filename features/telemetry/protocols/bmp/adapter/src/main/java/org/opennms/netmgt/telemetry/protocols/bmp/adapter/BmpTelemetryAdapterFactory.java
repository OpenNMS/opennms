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

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapterFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

public class BmpTelemetryAdapterFactory extends AbstractCollectionAdapterFactory {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public BmpTelemetryAdapterFactory() {
        super(null);
    }

    public BmpTelemetryAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return BmpTelemetryAdapter.class;
    }

    @Override
    public Adapter createBean(final AdapterDefinition adapterConfig) {
        final BmpTelemetryAdapter adapter = new BmpTelemetryAdapter(adapterConfig, this.getTelemetryRegistry().getMetricRegistry(), nodeDao, transactionTemplate);
        adapter.setCollectionAgentFactory(this.getCollectionAgentFactory());
        adapter.setPersisterFactory(this.getPersisterFactory());
        adapter.setFilterDao(this.getFilterDao());
        adapter.setPersisterFactory(this.getPersisterFactory());
        adapter.setInterfaceToNodeCache(this.getInterfaceToNodeCache());
        adapter.setThresholdingService(this.getThresholdingService());
        adapter.setBundleContext(this.getBundleContext());

        return adapter;
    }

}
