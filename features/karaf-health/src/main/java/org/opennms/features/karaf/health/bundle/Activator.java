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
package org.opennms.features.karaf.health.bundle;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.karaf.health.service.KarafHealthService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.FrameworkWiring;

public class Activator implements BundleActivator {

    private ServiceRegistration karafHealthServiceSvcReg;

    @Override
    public void start(BundleContext context) throws Exception {
        // Determine if any vaadin theme fragments are unresolved
        final Set<Bundle> unresolvedVaadinThemeFragments = getUnresolvedVaadinThemeFragments(context);
        if (!unresolvedVaadinThemeFragments.isEmpty()) {
            // Get vaadin theme host bundle to initialize refresh
            final Bundle vaadinThemeHostBundle = getVaadinThemeHostbundle(context);
            final List<Bundle> bundlesToRefresh = new ArrayList<>();
            bundlesToRefresh.add(vaadinThemeHostBundle);

            // Refresh
            final Bundle systemBundle = context.getBundle(0);
            FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class);
            frameworkWiring.refreshBundles(bundlesToRefresh);
        }

        KarafHealthServiceImpl karafHealthService = new KarafHealthServiceImpl();
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("registration.export", "true");
        karafHealthServiceSvcReg = context.registerService(KarafHealthService.class, karafHealthService, props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (karafHealthServiceSvcReg != null) {
            karafHealthServiceSvcReg.unregister();
        }
    }

    private static Bundle getVaadinThemeHostbundle(BundleContext context) {
        return Arrays.stream(context.getBundles())
                .filter(b -> "com.vaadin.themes".equalsIgnoreCase(b.getSymbolicName()))
                .findFirst()
                .orElse(null);
    }

    private static Set<Bundle> getUnresolvedVaadinThemeFragments(BundleContext context) {
        return Arrays.stream(context.getBundles())
                .filter(bundle -> {
                    String fragmentHost = bundle.getHeaders().get("Fragment-Host");
                    return fragmentHost != null && fragmentHost.contains("com.vaadin.themes") && bundle.getState() == Bundle.INSTALLED;
                })
                .collect(Collectors.toSet());
    }
}