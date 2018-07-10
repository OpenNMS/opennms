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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.opennms.distributed.core.health.Context;
import org.opennms.distributed.core.health.HealthCheck;
import org.opennms.distributed.core.health.Response;
import org.opennms.distributed.core.health.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class HealthCheckService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("health-check-%d").build());

    private BundleContext bundleContext;

    public HealthCheckService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    private List<HealthCheck> getHealthChecks() throws InvalidSyntaxException {
        final Collection<ServiceReference<HealthCheck>> serviceReferences = bundleContext.getServiceReferences(HealthCheck.class, null);
        if (serviceReferences.isEmpty()) {
            throw new IllegalStateException("No health checks available.");
        }
        return serviceReferences.stream().map(ref -> bundleContext.getService(ref)).collect(Collectors.toList());
    }

    // Perform check asynchronously
    public void performAsyncHealthCheck(Context context, Consumer<HealthCheck> onStartConsumer, Consumer<Response> onFinishConsumer) throws InvalidSyntaxException {
        final List<HealthCheck> checks = getHealthChecks();
        for (HealthCheck check : checks) {
            try {
                if (onStartConsumer != null) {
                    onStartConsumer.accept(check);
                }
                final CompletableFuture<Response> c = new CompletableFuture();
                executorService.submit(() -> {
                    try {
                        final Response response = check.perform(context);
                        if (response == null) {
                            c.complete(new Response(Status.Unknown));
                        }
                        c.complete(response);
                    } catch (Exception ex) {
                        c.complete(new Response(ex));
                    }
                });
                final Response response = c.get(context.getTimeout(), TimeUnit.MILLISECONDS);
                if (onFinishConsumer != null) {
                    onFinishConsumer.accept(response);
                }
            } catch (TimeoutException timeoutException) {
                onFinishConsumer.accept(new Response(Status.Timeout, "Health Check did not finish within " + context.getTimeout() + "ms"));
            } catch (Exception ex) {
                onFinishConsumer.accept(new Response(ex));
            }
        }
    }
}
