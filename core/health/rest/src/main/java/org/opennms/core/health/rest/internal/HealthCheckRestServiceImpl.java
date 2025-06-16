/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
