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
package org.opennms.container.web.bridge.proxy.trackers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.junit.Test;
import org.opennms.container.web.bridge.proxy.ProxyFilter;
import org.opennms.container.web.bridge.proxy.handlers.RequestHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServletTrackerTest {

    /**
     * The ReST endpoints are published as one servlet per application, i.e. one for all of /rest.
     * Registering a request handler for that pattern would divert the whole ReST API of the web
     * application into the OSGi container, so these servlets have to be ignored here - the
     * RestRequestHandler covers them at resource granularity instead.
     */
    @Test
    public void ignoresServletsOfTheJaxRsPublisher() {
        final ProxyFilter proxyFilter = mock(ProxyFilter.class);
        final ServiceReference<Servlet> reference =
                servletReference("org.opennms.container.bridge.rest", "/*");

        newTracker(proxyFilter, reference).addingService(reference);

        verify(proxyFilter, never()).addRequestHandler(any());
    }

    @Test
    public void tracksServletsOfOtherBundles() {
        final ProxyFilter proxyFilter = mock(ProxyFilter.class);
        final ServiceReference<Servlet> reference =
                servletReference("org.opennms.features.some.bundle", "/some-servlet/*");

        newTracker(proxyFilter, reference).addingService(reference);

        verify(proxyFilter).addRequestHandler(any(RequestHandler.class));
    }

    private static ServletTracker newTracker(ProxyFilter proxyFilter, ServiceReference<Servlet> reference) {
        final BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getService(reference)).thenReturn(mock(Servlet.class));
        return new ServletTracker(bundleContext, mock(ServletContext.class), proxyFilter);
    }

    @SuppressWarnings("unchecked")
    private static ServiceReference<Servlet> servletReference(String bundleSymbolicName, String pattern) {
        final Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn(bundleSymbolicName);
        final ServiceReference<Servlet> reference = mock(ServiceReference.class);
        when(reference.getBundle()).thenReturn(bundle);
        when(reference.getProperty("osgi.http.whiteboard.servlet.pattern")).thenReturn(pattern);
        return reference;
    }
}
