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
package org.opennms.osgi.locator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLocator {
    private final Logger Log = LoggerFactory.getLogger(getClass());

    public <T> T lookup(Class<T> lookupClass, BundleContext bundleContext) {
        if (bundleContext == null) throw new IllegalArgumentException("BundleContext must not be null!");
        if (lookupClass == null) throw new IllegalArgumentException("LookupClass must not be null!");
        try {
            ServiceReference<?>[] serviceReferences = bundleContext.getAllServiceReferences(
                    lookupClass.getName(),
                    String.format("(bundleId=%d)", bundleContext.getBundle().getBundleId()));
            if (serviceReferences == null || serviceReferences.length == 0) {
                Log.warn("No {} found", lookupClass);
                return null;
            }
            return (T) bundleContext.getService(serviceReferences[0]);
        } catch (InvalidSyntaxException ex) {
            Log.warn(String.format("Error while retrieving %s", lookupClass), ex);
        }
        Log.warn("No {} found", lookupClass);
        return null;
    }
}
