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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import java.util.Map;
import java.util.Optional;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapterFactory;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Maps;

public class BmpIntegrationAdapterFactory extends AbstractAdapterFactory {

    public BmpIntegrationAdapterFactory() {
        super(null);
    }

    public BmpIntegrationAdapterFactory(final BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return BmpIntegrationAdapter.class;
    }

    @Override
    public Adapter createBean(final AdapterDefinition adapterConfig) {
        // Find topic prefix
        final String topicPrefix = adapterConfig.getParameterMap().remove("topicPrefix");

        // Extract kafka producer config
        final Map<String, Object> kafkaConfig = Maps.newHashMap();
        adapterConfig.getParameterMap().entrySet().removeIf(e -> {
            final Optional<String> prefix = StringUtils.truncatePrefix(e.getKey(), "kafka.");
            prefix.ifPresent(k -> kafkaConfig.put(k, e.getValue()));
            return prefix.isPresent();
        });

        return new BmpIntegrationAdapter(adapterConfig,
                                         this.getTelemetryRegistry().getMetricRegistry(),
                                         new BmpKafkaProducer(topicPrefix, kafkaConfig));
    }
}
