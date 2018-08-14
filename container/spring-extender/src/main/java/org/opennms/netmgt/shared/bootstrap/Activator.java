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

package org.opennms.netmgt.shared.bootstrap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener;
import org.eclipse.gemini.blueprint.extender.internal.activator.NamespaceHandlerActivator;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.util.ReflectionUtils;


// Original the Activator simply loaded the ApplicationContext from the bundle.
// However it lacked the functionality to load the spring.handlers and other spring internals.
// Therefore we re-use the functionality from eclipse-blueprint to do so.
// See https://github.com/eclipse/gemini.blueprint/blob/6976b50e76a17aaf800a60f836c6b7af46fbf483/extender/src/main/java/org/eclipse/gemini/blueprint/extender/internal/boot/ChainActivator.java
public class Activator implements BundleActivator {

    private final BundleActivator[] activators;

    public Activator() {
        final NamespaceHandlerActivator activateCustomNamespaceHandling = new NamespaceHandlerActivator();
        final ExtenderConfiguration initializeExtenderConfiguration = new ExtenderConfiguration();
        final MyContextLoaderListener listenForSpringDmBundles = new MyContextLoaderListener(initializeExtenderConfiguration);

        activators = new BundleActivator[] {
                activateCustomNamespaceHandling,
                initializeExtenderConfiguration,
                listenForSpringDmBundles
        };
    }

    @Override
    public void start(BundleContext context) throws Exception {
        for (int i = 0; i < activators.length; i++) {
            activators[i].start(context);
        }

        // Manually trigger start of spring application context for this bundle
        ((MyContextLoaderListener)activators[activators.length - 1]).maybeCreateApplicationContextFor(context.getBundle());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (int i = activators.length - 1; i >= 0; i--) {
            activators[i].stop(context);
        }
    }

    // TODO MVR decide if we need this, or if this can be removed
    // UGLY HACK  !!
    // Gemini-listeners do not start application context within the activating bundle.
    // We use this implementation to manually invoke it
    private static class MyContextLoaderListener extends ContextLoaderListener {

        public MyContextLoaderListener(ExtenderConfiguration extenderConfiguration) {
            super(extenderConfiguration);
        }

        public void maybeCreateApplicationContextFor(Bundle bundle) {
            final Field field = ReflectionUtils.findField(getClass(), "lifecycleManager", null);
            ReflectionUtils.makeAccessible(field);
            Object target = ReflectionUtils.getField(field, this);
            Method method = ReflectionUtils.findMethod(target.getClass(), "maybeCreateApplicationContextFor", Bundle.class);
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, target, bundle);
        }
    }
}