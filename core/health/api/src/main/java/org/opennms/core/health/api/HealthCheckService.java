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

package org.opennms.core.health.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.vavr.control.Either;

/**
 * The {@link HealthCheckService} performs various {@link HealthCheck}s and returns
 * the result of each check wrapped by the {@link Health} object.
 *
 * @author mvrueden
 */
public interface HealthCheckService {

    /**
     * Performs various {@link HealthCheck}s asynchronously and returns a {@link CompletableFuture} which
     * contains the {@link Health} representing each {@link HealthCheck}s {@link Response}.
     *
     * It is up to the implementation to respect timeouts and handle exceptions accordingly, when
     * these situations arise when calling {@link HealthCheck#perform(Context)}.
     *
     * Callback methods can be provided to print information before and after a {@link HealthCheck} is invoked.
     * When providing a <code>onFinishConsumer</code> please note, that this is
     * invoked even if the {@link HealthCheck#perform(Context)} execution failed or timed out and therefore may not
     * represent the value when calling {@link HealthCheck#perform(Context)} directly.
     *
     * @param context The context object
     * @param listener gets informed about health check progress. May be null. In case the {@code Either} contains an
     *                 error no listener callbacks are called at all. If the {@code Either} contains a {@CompletionStage}
     *                 all listener callbacks are guaranteed to be called before that completion stage completes.
     * @param tags selects the single health checks that are included in the overall check; every health check that has any of the given tags is included.
     * @return Either an error message if no matching health checks could be determined or a {@link CompletableFuture} to retrieve the {@link Health} from.
     */
    Either<String, CompletionStage<Health>> performAsyncHealthCheck(Context context, ProgressListener listener, List<String> tags);

    interface ProgressListener {
        default void onHealthChecksFound(List<HealthCheck> checks) {};
        default void onPerform(HealthCheck check) {};
        default void onResponse(HealthCheck check, Response response) {};
        default void onAllHealthChecksCompleted(Health health) {};
    }
}
