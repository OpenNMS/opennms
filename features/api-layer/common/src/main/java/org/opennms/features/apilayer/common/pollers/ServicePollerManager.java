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
package org.opennms.features.apilayer.common.pollers;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.integration.api.v1.pollers.ServicePollerFactory;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager to plug the service pollers that implement integration-api to the default service poller registry.
 */
public class ServicePollerManager extends InterfaceMapper<ServicePollerFactory, ServiceMonitor> {

    private static final Logger LOG = LoggerFactory.getLogger(ServicePollerManager.class);

    public ServicePollerManager(BundleContext bundleContext) {
        super(ServiceMonitor.class, bundleContext);
    }

    @Override
    public ServiceMonitor map(ServicePollerFactory ext) {
        return new ServicePollerImpl(ext);
    }

    // override as registry needs poller class name in properties.
    @Override
    public Map<String, Object> getServiceProperties(ServicePollerFactory extension) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", extension.getPollerClassName());
        return properties;
    }

}
