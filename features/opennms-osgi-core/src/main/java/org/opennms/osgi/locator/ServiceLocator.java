/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
