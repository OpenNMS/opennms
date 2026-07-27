/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.container.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.opennms.container.daemon.KarafContext;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes the context of the OpenNMS-managed Karaf service to the servlet
 * bridge. Karaf lifecycle ownership remains outside the web application.
 */
public class WebAppListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(WebAppListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final BundleContext bundleContext = getBundleContext();
        if (bundleContext != null) {
            event.getServletContext().setAttribute(BundleContext.class.getName(), bundleContext);
        } else {
            // The context is read once, at web application startup: starting Karaf
            // afterwards does not enable proxying until the web application restarts.
            LOG.warn("Karaf is not running; OSGi servlet and resource proxying is disabled. "
                    + "Check that the Karaf service is enabled in service-configuration.xml.");
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        event.getServletContext().removeAttribute(BundleContext.class.getName());
    }

    protected BundleContext getBundleContext() {
        return KarafContext.getBundleContextIfAvailable().orElse(null);
    }
}
