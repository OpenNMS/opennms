/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.health.rest.internal;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.Health;
import org.opennms.core.health.api.HealthCheckService;
import org.opennms.core.health.rest.HealthCheckRestService;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckRestServiceImpl implements HealthCheckRestService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckRestServiceImpl.class);
    private static final String SUCCESS_MESSAGE = "Everything is awesome";
    private static final String ERROR_MESSAGE = "Oh no, something is wrong";

    private final HealthCheckService healthCheckService;

    public HealthCheckRestServiceImpl(final HealthCheckService healthCheckService) {
        this.healthCheckService = Objects.requireNonNull(healthCheckService);
    }

    @Override
    public Response probeHealth(int timeoutInMs, int maxAgeMs, UriInfo uriInfo) {
        List<String> tags = uriInfo.getQueryParameters().get("tag");
        var isSuccess = getHealthInternally(timeoutInMs, maxAgeMs, tags).fold(
                errorMessage -> false,
                health -> health.isSuccess()
        );
        if (isSuccess) {
            return Response.ok()
                    .header("Health", SUCCESS_MESSAGE)
                    .entity(SUCCESS_MESSAGE)
                    .build();
        }
        return Response.status(new UnhealthyStatusType())
                .header("Health", ERROR_MESSAGE)
                .entity(ERROR_MESSAGE)
                .build();
    }

    @Override
    public Response getHealth(int timeoutInMs, int maxAgeMs, UriInfo uriInfo) {
        List<String> tags = uriInfo.getQueryParameters().get("tag");
        var flagAndResponse = getHealthInternally(timeoutInMs, maxAgeMs, tags).fold(
                errorMessage -> {
                    final JSONObject jsonHealth = new JSONObject();
                    final JSONArray jsonResponseArray = new JSONArray();
                    jsonHealth.put("healthy", false);
                    jsonHealth.put("errorMessage", errorMessage);
                    jsonHealth.put("responses", jsonResponseArray);
                    return Pair.of(false, jsonHealth);
                },
                health -> {
                    final JSONObject jsonHealth = new JSONObject();
                    final JSONArray jsonResponseArray = new JSONArray();
                    jsonHealth.put("healthy", health.isSuccess());
                    jsonHealth.put("errorMessage", (String) null);
                    jsonHealth.put("responses", jsonResponseArray);
                    for (var pair : health.getResponses()) {
                        final JSONObject eachJsonResponse = new JSONObject();
                        eachJsonResponse.put("status", pair.getRight().getStatus().name());
                        eachJsonResponse.put("description", pair.getLeft().getDescription());
                        eachJsonResponse.put("message", pair.getRight().getMessage());
                        jsonResponseArray.put(eachJsonResponse);
                    }
                    return Pair.of(health.isSuccess(), jsonHealth);
                }
        );
        LOG.debug("Rest response : {}", flagAndResponse.getRight().toString());
        // Return response
        return Response.ok()
                .header("Health", flagAndResponse.getLeft() ? SUCCESS_MESSAGE : ERROR_MESSAGE)
                .entity(flagAndResponse.getRight().toString())
                .build();
    }

    private Either<String, Health> getHealthInternally(int timeoutInMs, int maxAgeMs, List<String> tags) {
        final Context context = new Context();
        context.setTimeout(timeoutInMs);
        context.setMaxAge(Duration.ofMillis(maxAgeMs));
        return healthCheckService.performAsyncHealthCheck(context, null, tags).map(f -> {
            try {
                return f.toCompletableFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
