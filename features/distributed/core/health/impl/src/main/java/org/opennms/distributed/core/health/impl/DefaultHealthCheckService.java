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

import org.opennms.distributed.core.health.Context;
import org.opennms.distributed.core.health.Health;
import org.opennms.distributed.core.health.HealthCheck;
import org.opennms.distributed.core.health.HealthCheckService;
import org.opennms.distributed.core.health.Response;
import org.opennms.distributed.core.health.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class DefaultHealthCheckService implements HealthCheckService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("health-check-%d").build());

    private BundleContext bundleContext;

    public DefaultHealthCheckService(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    private List<HealthCheck> getHealthChecks() throws InvalidSyntaxException {
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
