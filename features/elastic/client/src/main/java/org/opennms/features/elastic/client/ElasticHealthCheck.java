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
package org.opennms.features.elastic.client;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Health check for Elasticsearch connectivity using ElasticRestClient.
 */
public class ElasticHealthCheck implements HealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticHealthCheck.class);
    private static final String ELASTIC = "elastic";

    private final ElasticRestClient client;
    private final String featureName;

    public ElasticHealthCheck(ElasticRestClient client, String featureName) {
        this.client = Objects.requireNonNull(client);
        this.featureName = Objects.requireNonNull(featureName);
    }

    @Override
    public String getDescription() {
        return "Elasticsearch cluster health check for " + featureName;
    }

    @Override
    public List<String> getTags() {
        return List.of(ELASTIC);
    }

    @Override
    public Response perform(Context context) throws Exception {
        try {
            String clusterHealth = client.health();
            
            // Interpret the cluster health status
            if ("green".equalsIgnoreCase(clusterHealth) || "yellow".equalsIgnoreCase(clusterHealth)) {
                return new Response(Status.Success, 
                    String.format("Elasticsearch cluster health: %s for %s", clusterHealth, featureName));
            } else {
                return new Response(Status.Failure, 
                    String.format("Elasticsearch cluster is unhealthy (status: %s) for %s", clusterHealth, featureName));
            }
            
        } catch (IOException e) {
            LOG.error("Failed to check Elasticsearch health", e);
            return new Response(Status.Failure, 
                                "Failed to connect to Elasticsearch for " + featureName + ": " + e.getMessage());
        }
    }
}