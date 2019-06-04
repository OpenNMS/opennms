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

package org.opennms.core.health.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.Health;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.HealthCheckService;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * The "Default" implementation of the {@link HealthCheckService}.
 * It loads all available {@link HealthCheckService}s from the OSGi service registry.
 * If no checks are available, the overall health is "Unhealthy".
 *
 * @author mvrueden
 */
public class DefaultHealthCheckService implements HealthCheckService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHealthCheckService.class);

    // HealthChecks are performed asynchronously with this executor.
    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("health-check-%d").build());

    // Context to load the services
    private BundleContext bundleContext;

    public DefaultHealthCheckService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    // Resolve all HealthChecks from the OSGi registry
    protected List<HealthCheck> getHealthChecks() throws InvalidSyntaxException {
        final Collection<ServiceReference<HealthCheck>> serviceReferences = bundleContext.getServiceReferences(HealthCheck.class, null);
        return serviceReferences.stream()
                .sorted(Comparator.comparingLong(ref -> ref.getBundle().getBundleId()))
                .map(ref -> bundleContext.getService(ref))
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Health> performAsyncHealthCheck(Context context, Consumer<HealthCheck> onStartConsumer, Consumer<Response> onFinishConsumer) {
        final CompletableFuture<Health> returnFuture = new CompletableFuture<>();
        final Health health = new Health();
        final Consumer<Response> consumer = response -> {
            health.withResponse(response);
            onFinishConsumer.accept(response);
        };
        try {
            // Fail if no checks are available
            final List<HealthCheck> checks = getHealthChecks();
            if (checks == null || checks.isEmpty()) {
                health.setError("No Health Checks available");
            } else {
                runChecks(context, checks, onStartConsumer, consumer);
            }
        } catch (InvalidSyntaxException ex) {
            health.setError("Error while performing health checks: " + ex.getMessage());
        } finally {
            returnFuture.complete(health);
        }
        return returnFuture;
    }

    // Asynchronously run all checks
    private void runChecks(Context context, List<HealthCheck> checks, Consumer<HealthCheck> onStartConsumer, Consumer<Response> onFinishConsumer) {
        Future<Response> currentFuture = null;
        for (HealthCheck check : checks) {
            try {
                if (onStartConsumer != null) {
                    onStartConsumer.accept(check);
                }
                currentFuture = executorService.submit(() -> {
                    try {
                        final Response response = check.perform(context);
                        if (response == null) {
                            return new Response(Status.Unknown);
                        }
                        return response;
                    } catch (Exception ex) {
                        // Log the stack trace
                        LOG.warn("Health check {} failed with exception: {}", check, ex.getMessage(), ex);
                        return new Response(ex);
                    }
                });
                final Response response = currentFuture.get(context.getTimeout(), TimeUnit.MILLISECONDS);
                if (onFinishConsumer != null) {
                    onFinishConsumer.accept(response);
                }
            } catch (TimeoutException timeoutException) {
                if (currentFuture != null) {
                    currentFuture.cancel(true);
                }
                onFinishConsumer.accept(new Response(Status.Timeout, "Health Check did not finish within " + context.getTimeout() + " ms"));
            } catch (Exception ex) {
                if (currentFuture != null) {
                    currentFuture.cancel(true);
                }
                onFinishConsumer.accept(new Response(ex));
            }
        }
    }
}
