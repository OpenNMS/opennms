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
package org.opennms.osgi.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.opennms.osgi.EventRegistry;
import org.opennms.osgi.OnmsServiceManager;
import org.opennms.vaadin.extender.SessionListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each opennms-bundle which uses vaadin wants to listen to Session-Events
 * (such as sessionInitialized and sessionDestroyed), In addition we need a {@link org.opennms.osgi.OnmsServiceManager} service.
 * This bundle registers a {@link org.opennms.osgi.EventRegistry} and a {@link org.opennms.osgi.OnmsServiceManager} to the OSGI-Container at bundle
 * activation when the property "OnmsAutoExportServices" is set in the MANIFEST.MF file.
 *
 */
public class Activator implements BundleActivator, SynchronousBundleListener {

    private final Logger Log = LoggerFactory.getLogger(getClass());

    public void start(BundleContext context) throws Exception {
        context.addBundleListener(this);
    }

    public void stop(BundleContext context) throws Exception {
        context.removeBundleListener(this);
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event == null) return;
        if (BundleEvent.STARTING == event.getType() && shouldAutoExportOnmsServices(event.getBundle())) {
            autoExportDefaultServices(event.getBundle().getBundleContext());
        } else if (BundleEvent.STOPPED == event.getType()) {
            // we do not need to remove any services the underlying OSGi-container should
            // have handled that for us
        }
    }

    private void autoExportDefaultServices(BundleContext bundleContext) {
        final long bundleId = bundleContext.getBundle().getBundleId();
        Log.info("Auto export of default Services for bundle (id: {}) enabled", bundleId);

        final OnmsServiceManager serviceManager = new OnmsServiceManagerImpl(bundleContext);
        Dictionary<String,Object> props = new Hashtable<String,Object>();
        props.put("bundleId", bundleContext.getBundle().getBundleId());

        Log.info("Registering OnmsServiceManager and SessionListener for bundle (id: {})", bundleId);
        bundleContext.registerService(
                new String[]{OnmsServiceManager.class.getName(), SessionListener.class.getName()},
                serviceManager, props);
        Log.info("Registering EventRegistry for bundle (id: {})", bundleId);
        bundleContext.registerService(EventRegistry.class.getName(), new EventRegistry(bundleContext), props);
    }

    private boolean shouldAutoExportOnmsServices(Bundle bundle) {
        if (bundle == null) return false;
        String headerValue = bundle.getHeaders().get("OnmsAutoExportServices");
        if (headerValue == null) {
            return false;
        }
        return Boolean.valueOf(headerValue);
    }
}
