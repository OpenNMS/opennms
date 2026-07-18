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
package org.opennms.container.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Test;
import org.osgi.framework.BundleContext;

public class WebAppListenerTest {
    @Test
    public void onlyPublishesAndRemovesTheExistingBundleContext() {
        final BundleContext bundleContext = mock(BundleContext.class);
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletContextEvent event = new ServletContextEvent(servletContext);
        final WebAppListener listener = new WebAppListener() {
            @Override
            protected BundleContext getBundleContext() {
                return bundleContext;
            }
        };

        listener.contextInitialized(event);
        verify(servletContext).setAttribute(BundleContext.class.getName(), bundleContext);

        listener.contextDestroyed(event);
        verify(servletContext).removeAttribute(BundleContext.class.getName());
    }

    @Test
    public void allowsTheWebApplicationToStartWithoutKaraf() {
        final ServletContext servletContext = mock(ServletContext.class);
        final ServletContextEvent event = new ServletContextEvent(servletContext);
        final WebAppListener listener = new WebAppListener() {
            @Override
            protected BundleContext getBundleContext() {
                return null;
            }
        };

        listener.contextInitialized(event);

        verify(servletContext, never()).setAttribute(
                org.mockito.ArgumentMatchers.eq(BundleContext.class.getName()),
                org.mockito.ArgumentMatchers.any());
        verify(servletContext).log("Karaf is not running; OSGi servlet and resource proxying is disabled.");
    }
}
