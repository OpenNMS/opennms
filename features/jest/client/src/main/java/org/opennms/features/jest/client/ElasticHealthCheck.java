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
package org.opennms.features.jest.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

import com.google.common.base.Strings;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Ping;

import static org.opennms.core.health.api.HealthCheckConstants.ELASTIC;

/**
 * Verifies the connection to ElasticSearch.
 * The health check may be located in an odd place for now.
 * The reason for this is, that multiple Modules create their own clients.
 * In order to not configure the client for the health check module as well, this {@link HealthCheck} is
 * only validating if the connection to ElasticSearch from the view of the flows/elastic bundle is working.
 *
 * @author mvrueden
 */
public class ElasticHealthCheck implements HealthCheck {

    private final JestClient client;

    private final String featureName;

    public ElasticHealthCheck(JestClient jestClient, String featureName) {
        this.client = Objects.requireNonNull(jestClient);
        this.featureName = Objects.requireNonNull(featureName);
    }

    @Override
    public String getDescription() {
        return "Connecting to ElasticSearch ReST API (" + featureName + ")";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(ELASTIC);
    }

    @Override
    public Response perform(Context context) {
        final Ping ping = new Ping.Builder().build();
        try {
            final JestResult result = client.execute(ping);
            if (result.isSucceeded() && Strings.isNullOrEmpty(result.getErrorMessage())) {
                return new Response(Status.Success);
            } else {
                return new Response(Status.Failure, Strings.isNullOrEmpty(result.getErrorMessage()) ? null : result.getErrorMessage());
            }
        } catch (IOException e) {
            return new Response(e);
        }
    }
}
