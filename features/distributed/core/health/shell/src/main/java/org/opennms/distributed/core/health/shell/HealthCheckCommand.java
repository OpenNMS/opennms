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

package org.opennms.distributed.core.health.shell;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.distributed.core.health.Context;
import org.opennms.distributed.core.health.HealthCheck;
import org.opennms.distributed.core.health.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@Command(scope = "health", name = "check", description="Verifies that the container is healthy.")
@Service
public class HealthCheckCommand implements Action {

    private static final String DESC_FORMAT = "%%-%ds   ";
    private static final String STATUS_FORMAT = "[ %%-%ds ]";

    @Option(name = "-t", description = "Maximum number of milliseconds to wait before failing when waiting for a check to complete (e.g. try to establish a JMS session.")
    public long timeout = 5L * 1000L;

    @Reference
    private BundleContext bundleContext;

    @Override
    public Object execute() throws Exception {
        final Context context = new Context();
        context.setTimeout(timeout);

        System.out.println("Verifying the health of the container");
        performHealthCheck(bundleContext, context);
        return null;
    }

    private static void performHealthCheck(BundleContext bundleContext, Context context) throws InvalidSyntaxException {
        final Collection<ServiceReference<HealthCheck>> serviceReferences = bundleContext.getServiceReferences(HealthCheck.class, null);
        if (serviceReferences.isEmpty()) {
            throw new IllegalStateException("No health checks available.");
        }

        // Determine max length for visualization
        final List<HealthCheck> healthChecks = serviceReferences.stream().map(s -> bundleContext.getService(s)).collect(Collectors.toList());
        final int maxColorLength = Arrays.stream(Color.values()).map(c -> c.toAnsii()).max(Comparator.comparingInt(String::length)).get().length();
        final int maxDescriptionLength = healthChecks.stream().map(check -> check.getDescription()).max(Comparator.comparingInt(String::length)).get().length();
        final int maxStatusLength = Arrays.stream(Status.values()).map(v -> v.name()).max(Comparator.comparingInt(String::length)).get().length() + maxColorLength + "\033[m".length() * 2 + Color.NoColor.toAnsii().length();
        final String descFormat = String.format(DESC_FORMAT, maxDescriptionLength);
        final String statusFormat = String.format(STATUS_FORMAT, maxStatusLength);

        // Run Health Check
        new HealthCheckService(bundleContext)
                .performAsyncHealthCheck(context,
                        healthCheck -> System.out.print(String.format(descFormat, healthCheck.getDescription())),
                        response -> {
                            final Status status = response.getStatus();
                            final Color statusColor = determineColor(status);
                            final String statusText = String.format(statusFormat, Colorizer.colorize(status.name(), statusColor));
                            System.out.print(statusText);
                            if (response.getMessage() != null) {
                                System.out.print(" => " + response.getMessage());
                            }
                            System.out.println();
                        });
    }

    private static Color determineColor(Status status) {
        switch (status) {
            case Failure: return Color.Red;
            case Timeout: return Color.Yellow;
            case Starting: return Color.Blue;
            case Success: return Color.Green;
            case Unknown: return Color.Yellow;
            default:      return Color.NoColor;
        }
    }
}
