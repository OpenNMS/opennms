/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.components.bundlerefresher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.FrameworkWiring;


public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        // Determine if any vaadin theme fragments are unresoled
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
    }

    @Override
    public void stop(BundleContext context) throws Exception {

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