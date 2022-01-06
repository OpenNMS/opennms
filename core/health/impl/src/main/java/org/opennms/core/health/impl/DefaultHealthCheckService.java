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

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.core.concurrent.FutureUtils;
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

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.vavr.control.Either;

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
    public Either<String, CompletionStage<Health>> performAsyncHealthCheck(Context context, ProgressListener listener, List<String> tags) {
        try {
            // Fail if no checks are available
            List<HealthCheck> checks = getHealthChecks();
            checks = filterChecksWithTags(checks, tags);
            if (checks == null || checks.isEmpty()) {
                return Either.left("No Health Checks available");
            } else {
                if (listener != null) {
                    listener.onHealthChecksFound(checks);
                }
                return Either.right(runChecks(context, checks, listener));
            }
        } catch (InvalidSyntaxException ex) {
            return Either.left("Error while performing health checks: " + ex.getMessage());
        }
    }

    List<HealthCheck> filterChecksWithTags(List<HealthCheck> checks, List<String> tags) {
        if (checks != null && tags != null && tags.stream().anyMatch(tag -> !Strings.isNullOrEmpty(tag))) {
            checks = checks.stream().filter(check -> check.getTags().stream().anyMatch(tags::contains)).collect(Collectors.toList());
        }
        return checks;
    }

    /**
     * Creates a completable future for the given health check.
     * <p>
     * The returned future is guaranteed to complete within the timeout given in the context instance. Listener
     * callbacks have been called before the future completes.
     */
    private CompletionStage<Pair<HealthCheck, Response>> completionStage(HealthCheck check, Context context, ProgressListener listener) {
        return FutureUtils.completionStageWithDefaultOnTimeout(
                () -> {
                    try {
                        if (listener != null) {
                            listener.onPerform(check);
                        }
                        var response = check.perform(context);
                        return response != null ? response : Response.UNKNOWN;
                    } catch (Throwable t) {
                        return new Response(t);
                    }
                },
                Duration.ofMillis(context.getTimeout()),
                () -> new Response(Status.Timeout, "Health Check did not finish within " + context.getTimeout() + " ms"),
                executorService
        ).thenApply(response -> {
            if (listener != null) {
                listener.onResponse(check, response);
            }
            return Pair.of(check, response);
        });
    }

    // Asynchronously run all checks
    private CompletionStage<Health> runChecks(Context context, List<HealthCheck> checks, ProgressListener listener) {
        return FutureUtils.traverse(checks, check -> completionStage(check, context, listener)).thenApply(list -> {
            var health = new Health(list);
            if (listener != null) {
                listener.onAllHealthChecksCompleted(health);
            }
            return health;
        });
    }
}
