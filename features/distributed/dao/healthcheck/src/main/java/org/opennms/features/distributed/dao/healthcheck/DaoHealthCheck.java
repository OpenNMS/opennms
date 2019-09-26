/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.dao.healthcheck;

import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Verifies that at least the NodeDao can be consumed as a OSGi service, otherwise
 * it is considered a Failure.
 * This is necessary to ensure that in case of running the DAOs inside sentinel they
 * were exposed through the spring extender as the bundle may be ACTIVE but no spring services were
 * exposed.
 *
 * @author mvrueden
 */
public class DaoHealthCheck implements HealthCheck {

    private BundleContext bundleContext;

    public DaoHealthCheck(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getDescription() {
        return "Retrieving NodeDao";
    }

    @Override
    public Response perform(Context context) {
        // IMPORTANT: Do not change to Class reference here. This is a string on purpose to not have the maven bundle
        // plugin put a IMPORT-Package statement for org.opennms.netmgt.dao.api. Otherwise this Health Check is never
        // loaded and will never be invoked, thus can not fullfil its purpose.
        final ServiceReference serviceReference = bundleContext.getServiceReference("org.opennms.netmgt.dao.api.NodeDao");
        if (serviceReference != null) {
            return new Response(Status.Success);
        } else {
            return new Response(Status.Failure, "No NodeDao available");
        }
    }
}
