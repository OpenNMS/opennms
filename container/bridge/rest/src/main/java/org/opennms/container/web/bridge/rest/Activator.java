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
package org.opennms.container.web.bridge.rest;

import org.opennms.container.web.bridge.api.RestEndpointRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Starts the {@link JaxRsPublisher} and exposes its endpoints to the web bridge as a
 * {@link RestEndpointRegistry}.
 *
 * A plain activator rather than Declarative Services: the publisher has no service dependencies of
 * its own - it tracks services itself - and this keeps the ReST endpoints independent of the SCR
 * extender being up.
 */
public class Activator implements BundleActivator {

    private JaxRsPublisher publisher;
    private ServiceRegistration<RestEndpointRegistry> registration;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        publisher = new JaxRsPublisher(bundleContext);
        publisher.start();
        registration = bundleContext.registerService(
                RestEndpointRegistry.class, new RestEndpointRegistryImpl(publisher), null);
    }

    @Override
    public void stop(final BundleContext bundleContext) {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        if (publisher != null) {
            publisher.stop();
            publisher = null;
        }
    }
}
