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
package org.opennms.netmgt.shared.bootstrap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Loads Spring application contexts for bundles carrying a {@code Spring-Context}
 * manifest header (distributed dao-impl, service-registry, ...).
 *
 * This used to delegate to the Eclipse Gemini Blueprint extender, which is not
 * runtime-compatible with Spring 5.x. It is now a minimal extender of our own:
 * a bundle tracker creates a plain Spring application context per bundle, namespace
 * handlers and schemas are resolved across all installed bundles, and the few
 * {@code osgi:reference} imports used by our contexts are honored by waiting for
 * the referenced OSGi service.
 */
public class Activator implements BundleActivator {

    private NamespaceProviderRegistry namespaceProviderRegistry;
    private SpringContextTracker springContextTracker;

    @Override
    public void start(BundleContext context) {
        namespaceProviderRegistry = new NamespaceProviderRegistry(context);
        namespaceProviderRegistry.open();
        springContextTracker = new SpringContextTracker(context, namespaceProviderRegistry);
        springContextTracker.open();
    }

    @Override
    public void stop(BundleContext context) {
        if (springContextTracker != null) {
            springContextTracker.close();
            springContextTracker = null;
        }
        if (namespaceProviderRegistry != null) {
            namespaceProviderRegistry.close();
            namespaceProviderRegistry = null;
        }
    }
}
