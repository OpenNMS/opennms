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

package org.opennms.distributed.core.health.impl;

import static org.opennms.distributed.core.health.Status.Failure;
import static org.opennms.distributed.core.health.Status.Starting;

import java.lang.management.ManagementFactory;
import java.util.Objects;

import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.opennms.distributed.core.health.Context;
import org.opennms.distributed.core.health.Health;
import org.opennms.distributed.core.health.HealthCheck;
import org.opennms.distributed.core.health.Response;
import org.opennms.distributed.core.health.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;

public class ContainerIntegrityHealthCheck implements HealthCheck {

    private final BundleService bundleService;
    private final BundleContext bundleContext;

    public ContainerIntegrityHealthCheck(BundleContext bundleContext, BundleService bundleService) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.bundleService = Objects.requireNonNull(bundleService);
    }

    @Override
    public String getDescription() {
        return "Verifying installed bundles";
    }

    @Override
    public Response perform(Context context) throws Exception {
        // Don't check within this delay period, because the container may not be started yet
        if (ManagementFactory.getRuntimeMXBean().getUptime() <= 10000) {
            return new Response(Starting, "Container is in spin up phase");
        }

        // Verify all bundles
        final Health health = new Health();
        for (Bundle b : bundleContext.getBundles()) {
            final BundleInfo info = bundleService.getInfo(b);
            switch (info.getState()) {
                // Success
                case Active:
                    break;
                // only success if bundle is a fragment bundle
                case Resolved:
                    if ((b.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
                        break;
                    }
                    health.add(new Response(Failure, "Bundle " + b.getBundleId() + " is resolved, but not active"));
                    break;
                case Waiting:
                case GracePeriod:
                    health.add(new Response(Starting, "Bundle " + b.getBundleId() + " is waiting for dependencies"));
                    break;
                case Installed:
                    health.add(new Response(Starting, "Bundle " + b.getBundleId() + " is not yet started"));
                    break;
                case Starting:
                    health.add(new Response(Starting, "Bundle " + b.getBundleId() + " is starting"));
                    break;
                case Stopping:
                case Failure:
                case Unknown:
                    health.add(new Response(Failure, "Bundle " + b.getBundleId() + " is not started"));
                    break;
            }
        }

        // If there are some issues, we return the worst one, otherwise everything is okay
        return health.getWorst().orElse(new Response(Status.Success));
    }
}
