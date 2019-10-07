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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
     * It is up to the implementator to respect timeouts and handle exceptions accordingly, when
     * these situations arise when calling {@link HealthCheck#perform(Context)}.
     *
     * Callback methods can be provided to print information before and after a {@link HealthCheck} is invoked.
     * When providing a <code>onFinishConsumer</code> please note, that this is
     * invoked even if the {@link HealthCheck#perform(Context)} execution failed or timed out and therefore may not
     * represent the value when calling {@link HealthCheck#perform(Context)} directly.
     *
     * @param context The context object
     * @param onStartConsumer Callback method which is invoked before the {@link HealthCheck#perform(Context)} method is invoked. May be null.
     * @param onFinishConsumer Callback method which is invoked after the {@link HealthCheck#perform(Context)} method is invoked. May be null.
     * @return The {@link CompletableFuture} to retrieve the {@link Health} from.
     */
    CompletableFuture<Health> performAsyncHealthCheck(Context context, Consumer<HealthCheck> onStartConsumer, Consumer<Response> onFinishConsumer);
}
