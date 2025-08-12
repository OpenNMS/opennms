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

import java.io.IOException;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * {@link ElasticHealthCheck} that requires configuration to be present in order to actually verify the connection.
 * This is required as some features may be installed but the connection to Elasticsearch may not be configured yet,
 * meaning the <code>opennms:health-check</code> would always fail, which may not be the desired behaviour.
 */
public class RequireConfigurationElasticHealthCheck extends ElasticHealthCheck {

    private final ConfigurationAdmin configAdmin;
    private final String pid;

    public RequireConfigurationElasticHealthCheck(final ElasticRestClient client, final String featureName, final ConfigurationAdmin configAdmin, final String pid) {
        super(client, featureName);
        this.configAdmin = Objects.requireNonNull(configAdmin);
        this.pid = Objects.requireNonNull(pid);
    }

    @Override
    public Response perform(Context context) throws Exception {
        // If not configured, make it unknown
        try {
            final Configuration configuration = configAdmin.getConfiguration(pid);
            if (configuration.getProperties() == null) {
                return new Response(Status.Success, "Not configured");
            }
        } catch (IOException e) {
            return new Response(e);
        }

        // Connection to Elastic is configured, now perform the check
        return super.perform(context);
    }
}