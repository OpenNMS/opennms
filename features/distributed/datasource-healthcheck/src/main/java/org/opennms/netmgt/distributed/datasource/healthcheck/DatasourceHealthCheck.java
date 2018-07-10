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

package org.opennms.netmgt.distributed.datasource.healthcheck;

import java.util.Objects;

import org.opennms.distributed.core.health.Context;
import org.opennms.distributed.core.health.HealthCheck;
import org.opennms.distributed.core.health.Response;
import org.opennms.distributed.core.health.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DatasourceHealthCheck implements HealthCheck {

    private BundleContext bundleContext;

    public DatasourceHealthCheck(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getDescription() {
        return "Retrieving Datasource";
    }

    @Override
    public Response perform(Context context) {
        final ServiceReference serviceReference = bundleContext.getServiceReference("javax.sql.DataSource");
        if (serviceReference != null) {
            return new Response(Status.Success);
        } else {
            return new Response(Status.Failure, "No javax.sql.DataSource service available");
        }
    }
}
