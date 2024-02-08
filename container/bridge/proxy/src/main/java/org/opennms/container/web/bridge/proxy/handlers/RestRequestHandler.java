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
package org.opennms.container.web.bridge.proxy.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.container.web.bridge.api.RestEndpointRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RestRequestHandler implements RequestHandler {

    private final BundleContext bundleContext;

    public RestRequestHandler(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canHandle(String requestedPath) {
        final List<String> knownPatterns = getPatterns();
        for (String eachPattern : knownPatterns) {
            if (requestedPath.startsWith(eachPattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getPatterns() {
        final ServiceReference<RestEndpointRegistry> serviceReference = bundleContext.getServiceReference(RestEndpointRegistry.class);
        if (serviceReference != null) {
            try {
                final RestEndpointRegistry restEndpointRegistry = bundleContext.getService(serviceReference);
                return restEndpointRegistry.getRestEndpoints();
            } finally {
                bundleContext.ungetService(serviceReference);
            }
        }
        return Collections.emptyList();
    }
}
